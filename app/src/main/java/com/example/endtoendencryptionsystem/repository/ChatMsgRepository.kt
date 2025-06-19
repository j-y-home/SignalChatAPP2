package com.example.endtoendencryptionsystem.repository

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.TypeReference
import com.example.endtoendencryptionsystem.entiy.database.ChatConversation
import com.example.endtoendencryptionsystem.entiy.database.ChatMetadata
import com.example.endtoendencryptionsystem.entiy.database.Group
import com.example.endtoendencryptionsystem.entiy.database.GroupChatMessage
import com.example.endtoendencryptionsystem.entiy.database.PrivateChatMessage
import com.example.endtoendencryptionsystem.entiy.dto.PrivateMessageDTO
import com.example.endtoendencryptionsystem.entiy.vo.PrivateMessageVO
import com.example.endtoendencryptionsystem.enums.ConversationType
import com.example.endtoendencryptionsystem.enums.MessageStatus
import com.example.endtoendencryptionsystem.http.RxSchedulers
import com.example.endtoendencryptionsystem.http.response.BusinessException
import com.example.endtoendencryptionsystem.utils.EncryptionUtil
import com.example.endtoendencryptionsystem.utils.isOnline

import com.example.endtoendencryptionsystem.utils.json
import com.example.endtoendencryptionsystem.utils.toJSONString
import com.example.endtoendencryptionsystem.utils.toObject
import com.example.endtoendencryptionsystem.utils.toPrivateChatMessage
import com.tencent.mmkv.MMKV
import com.wumingtech.at.handler.handleGlobalError
import com.wumingtech.at.http.ApiFactory
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import okhttp3.internal.userAgent
import kotlin.text.toLong
import kotlin.toString


/**
 * 聊天的repository
 */
class ChatMsgRepository(val app: Application) {

    private var db = AppDatabase.getDatabase(app)
    private val friendsDao = db.friendDao()
    private val privateMessageDao = db.privateMessageDao()
    private val metadataDao = db.metadataDao()
    private val chatConversationDao = db.chatConversationDao()
    private val privateChatMessageDao = db.privateChatMessageDao()
    private val groupChatMessageDao = db.groupChatMessageDao()
    private val groupDao = db.groupDao()
    private val TAG: String = "ChatRepository"

    /**
     * 获取对话
     */
    fun getAllConversations(): Flowable<List<ChatConversation>>{
        val userId = MMKV.defaultMMKV().decodeInt("userId").toLong()
        return chatConversationDao.getAllConversations(userId)
    }
    /**
     * 获取对话
     */
    fun getConversationByUserIdAndTargetIdAndType(targetId:Long,type:String): Flowable<ChatConversation>{
        val userId = MMKV.defaultMMKV().decodeInt("userId").toLong()
        return Flowable.create({
            var chat = chatConversationDao.getConversation(userId,targetId,type)
            if(chat == null){
                //创建会话
                val conversation = ChatConversation()
                conversation.userId = userId
                conversation.targetId = targetId
                conversation.type = type
                conversation.lastContent = ""
                val newChatId = chatConversationDao.insertConversation(conversation)
                conversation.id = newChatId
                chat = conversation
            }
            it.onNext(chat)
            it.onComplete()
        }, BackpressureStrategy.ERROR)
    }
    /**
     * 获取某个对话的私聊的消息
     */
    fun getPrivateMsgByConversationId(conversationId:Long): Flowable<List<PrivateChatMessage>>{
        return privateChatMessageDao.getMessagesByConversation(conversationId)
    }

    /**
     * 发送私聊消息：
     * userId+type+targetId确定唯一会话
     * 先判断会话是否存在，不存在则创建，存在则获取会话Id，并更新
     * body为原始消息，
     * 1，加密消息
     * 2，请求接口发送
     * 3，保存原始消息到本地数据库
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun sendPrivateMessage(body: PrivateMessageDTO): Flowable<Boolean>{
        return if (app.isOnline()) {
            //先加密，再发送
            val origanlMsg = body.content
            val enMsg = EncryptionUtil.encryptPrivateMessage(body.recvId.toString(), origanlMsg)
            if(enMsg!=null){
                body.content = enMsg
                ApiFactory.API.api.sendPrivateMsg(body)
                    .compose(RxSchedulers.ioToMain())
                    .compose(handleGlobalError(app))
                    .compose(RxSchedulers.mainToIo())
                    .flatMap{
                        //保存原始消息到本地数据库
                        it.content = origanlMsg
                        saveChatConversation(it, ConversationType.PRIVATE.type)
                        return@flatMap Flowable.just(true)
                    }
            }else{
                Flowable.create({
                    it.onError(Throwable("加密消息失败：会话不存在"))
                }, BackpressureStrategy.ERROR)
            }
        } else {
            Flowable.create({
                it.onError(Throwable("请在网络良好的条件下发送消息"))
            }, BackpressureStrategy.ERROR)
        }
    }

    /**
     * 保存会话
     * @param it 消息体
     * @param type 会话类型 ("PRIVATE" 或 "GROUP")
     * userId:我的id
     * targetId:对方id
     * content：最新的消息内容
     * type:会话类型 ("PRIVATE" 或 "GROUP")
     * sendNickName：对方昵称
     * lastSendTime：最新消息发送时间
     */
    fun saveChatConversation(it:PrivateMessageVO,type:String){
        //1，判断会话是否存在
        var conversationId: Long = 0
        val userId:Long = MMKV.defaultMMKV().decodeInt("userId").toLong()
        var targetId = if(it.sendId == userId){it.recvId} else{it.sendId}
        var sendNickName = if(it.sendId == userId){//TODO
           "我"
        }else{
            "对方"
        }
        val existingConversation: ChatConversation? = chatConversationDao.getConversation(userId, targetId, type.toString())
        if (existingConversation != null) {//更新会话
            //更新最新一条消息
            existingConversation.lastContent = it.content.toString()
            existingConversation.lastSendTime = it.sendTime.time
            chatConversationDao.updateConversation(existingConversation)
            conversationId = existingConversation.id
        } else {//创建会话
            val conversation = ChatConversation()
            conversation.userId = userId
            conversation.sendNickName = sendNickName
            conversation.targetId = targetId
            conversation.type = type
            conversation.lastContent = it.content.toString()
            conversation.lastSendTime = it.sendTime.time
            try{
                Log.e("xxx","新增会话："+json.toJSONString(conversation))
                conversationId = chatConversationDao.insertConversation(conversation)
            }catch (ex: Exception){
                Log.e("xxx","插入数据失败："+ex.message)
            }

        }
        //2，保存消息到数据库
        saveOneMessage(json.toJSONString(it), conversationId, type)
    }


    /**
     * 保存最新消息到数据库私聊或者群聊表
     * @param messageMap 消息数据Map
     * @param conversationId 会话ID
     * @param type 会话类型 ("PRIVATE" 或 "GROUP")
     */
    private fun saveOneMessage(messageJson: String, conversationId: Long, type: String?) {
        try {
            // 根据会话类型选择不同的DAO
            if (ConversationType.PRIVATE.type == type) {
                var message = json.toObject<PrivateChatMessage>(messageJson)
                // 跳过时间提示消息等特殊消息
                if(message.type == 20){
                    return
                }
                message.conversationId = conversationId
                //处理有些消息的messageId为空的情况
                val msgJson = JSONObject.parseObject(messageJson)
                if(message.messageId.isNullOrEmpty()){
                    message.messageId = msgJson.getString("id")?:""
                }
                message.isSelfSend = message.sendId == MMKV.defaultMMKV().decodeInt("userId").toLong()
                var existingMessage = privateChatMessageDao.getMessagesById(message.messageId)
                if (existingMessage != null) {
                    privateChatMessageDao.updateMessage(message)
                } else {
                    val insertedId: Long = privateChatMessageDao.insertMessage(message)
                }
            } else if (ConversationType.GROUP.type == type) {
                val msgJson = JSONObject.parseObject(messageJson)
                // 处理 @用户ID
                if (msgJson.containsKey("atUserIds")) {
                    val atUserIds = msgJson.getJSONArray("atUserIds")
                    val atUserIdsStr = StringBuilder()
                    for (k in atUserIds.indices) {
                        if (k > 0) {
                            atUserIdsStr.append(",")
                        }
                        atUserIdsStr.append(atUserIds.getLong(k))
                    }
                    msgJson.put("atUserIds",atUserIdsStr.toString())
                }
                var message = msgJson.toJavaObject(GroupChatMessage::class.java)
                // 跳过时间提示消息等特殊消息
                if(message.type == 20){
                    return
                }
                message.conversationId = conversationId
                //处理有些消息的messageId为空的情况
                if(message.messageId.isEmpty()){
                    message.messageId = JSONObject.parseObject(messageJson).getString("id")?:""
                }
                Log.e(TAG ,"@的用户："+message.atUserIds)
                var existingMessage = groupChatMessageDao.getMessagesById(message.messageId)
                if (existingMessage != null) {
                    groupChatMessageDao.updateMessage(message)
                } else {
                    val insertedId: Long = groupChatMessageDao.insertMessage(message)
                }
            }
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Error saving message: " + e.message, e)
        }
    }


    /**
     * 更新会话
     * @param conversation 会话实体
     * @param messageMap 消息数据Map
     */
    fun updateChat(conversation: ChatConversation) {
        chatConversationDao.updateConversation(conversation)
    }

    /**
     * 更新消息已读状态
     * @param conversation 会话实体
     * @param messageMap 消息数据Map
     */
    fun updateMessageReadStatus(messageIds: ArrayList<String>) : Boolean {
        return try {
            db.runInTransaction {
                // 1. 更新私聊消息表中的消息状态
                privateChatMessageDao.updateMessagesReadStatus(messageIds, MessageStatus.READED.code)
                // 2. 更新会话表中的未读计数和@提醒状态  TODO 需要吗？
//                chatSessionDao.resetUnreadCount(userId, friendId, "PRIVATE")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update messages read status", e)
            false
        }
    }


        /**
     * 删除会话
     * @param conversation 会话实体
     * @param messageMap 消息数据Map
     */
    fun deleteChat(chatId:Long, chatType: String): Boolean {
        return try {
            // 使用事务确保原子性操作
            db.runInTransaction {
                // 1. 删除会话
                chatConversationDao.deleteConversationById(chatId)

                // 2. 删除关联的消息
                if (chatType == "PRIVATE") {
                    privateChatMessageDao.deleteMessagesByChatId(chatId)
                } else if (chatType == "GROUP") {
                    groupChatMessageDao.deleteMessagesByChatId(chatId)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete chat and messages", e)
            false
        }
    }

    /**
     * 加载会话消息
     * @param type 会话类型
     * @param targetId 目标ID
     * @param limit 消息数量限制
     * @param offset 偏移量（用于分页）
     */
    fun loadMessages(userId: Long,type: String?, targetId: Long, limit: Int, offset: Int): String? {
        try {
            val conversation: ChatConversation? =
                chatConversationDao.getConversation(userId,targetId,type)
            if (conversation == null) {
                return "[]"
            }

            val messages: MutableList<Any?> = ArrayList<Any?>()
            if ("PRIVATE" == type) {
                val privateMessages: MutableList<PrivateChatMessage?> =
                        privateChatMessageDao.getMessagesForConversation(conversation.id, offset,limit)
                for (msg in privateMessages) {
                    messages.add(msg)
                }
            } else if ("GROUP" == type) {
                // 类似处理群聊消息...
                val groupChatMessages: MutableList<GroupChatMessage?> =
                    groupChatMessageDao.getMessagesForConversation(conversation.id, offset,limit)
                for (msg in groupChatMessages) {
                    messages.add(msg)
                }
            }
            return json.toJSONString(messages)
        } catch (e: java.lang.Exception) {
            Log.e("ChatStorageManager", "Error loading messages: " + e.message)
            return "[]"
        }
    }

    /**
     * 保存会话
     */
    fun saveChats(userId: Long, chatsToSave: JSONArray){
        try {
            for (i in 0..<chatsToSave.size) {
                val chatJson: JSONObject = chatsToSave.getJSONObject(i)
                // 创建会话对象
                val conversation: ChatConversation = ChatConversation()
                conversation.userId = userId
                conversation.type = chatJson.getString("type")?: ""
                conversation.targetId = chatJson.getLong("targetId")
                conversation.showName = chatJson.getString("showName")
                conversation.headImage = chatJson.getString("headImage")
                conversation.lastContent = chatJson.getString("lastContent")
                conversation.lastSendTime = chatJson.getLong("lastSendTime")
                conversation.unreadCount = chatJson.getIntValue("unreadCount")
                conversation.isAtMe = chatJson.getBooleanValue("atMe")
                conversation.isAtAll = chatJson.getBooleanValue("atAll")
                conversation.lastTimeTip = chatJson.getLong("lastTimeTip")
                conversation.sendNickName = chatJson.getString("sendNickName")?: ""
                // 保存会话，获取会话ID
                val conversationId: Long
                val existingConversation: ChatConversation? = chatConversationDao.getConversation(
                        userId, conversation.targetId!!, conversation.type
                )

                if (existingConversation != null) {
                    conversation.id = existingConversation.id
                    chatConversationDao.updateConversation(conversation)
                    conversationId = existingConversation.id
                } else {
                    conversationId = chatConversationDao.insertConversation(conversation)
                }

                // 处理消息
                if (chatJson.containsKey("messages")) {
                    val messagesObj = chatJson.getJSONObject("messages")
                    val messagesArray = JSONArray()
                    // 将对象形式的消息转换为数组形式
                    for (key in messagesObj.keys) {
                        messagesArray.add(messagesObj.getJSONObject(key))
                    }
                    if ("PRIVATE" == conversation.type) {
                        savePrivateMessages(messagesArray, conversationId)
                    } else if ("GROUP" == conversation.type) {
                        saveGroupMessages(messagesArray, conversationId)
                    }
                }
            }
        } catch (ex: Exception){
            Log.e("ChatRepository", "Error saving chats: ${ex.message}")
        }
    }

    // 辅助方法：保存私聊消息
    private fun savePrivateMessages(messagesArray: JSONArray, conversationId: Long) {
        for (j in messagesArray.indices) {
            val msgJson = messagesArray.getJSONObject(j)

            val message: PrivateChatMessage = PrivateChatMessage()
            message.conversationId = conversationId
            // 处理可能缺失的字段
            if (msgJson.containsKey("messageId")) {
                message.messageId = msgJson.getString("messageId")
            }
            if (msgJson.containsKey("tmpId")) {
                message.tmpId = msgJson.getString("tmpId")
            }
            if (msgJson.containsKey("sendId")) {
                message.sendId = msgJson.getLong("sendId")
            }
            if (msgJson.containsKey("recvId")) {
                message.recvId = msgJson.getLong("recvId")
            }
            if (msgJson.containsKey("content")) {
                message.content = msgJson.getString("content")
            }
            if (msgJson.containsKey("sendTime")) {
                message.sendTime = msgJson.getLong("sendTime")
            }
            if (msgJson.containsKey("selfSend")) {
                message.isSelfSend = msgJson.getBooleanValue("selfSend")
            }
            if (msgJson.containsKey("type")) {
                message.type = msgJson.getIntValue("type")
            }
            if (msgJson.containsKey("status")) {
                message.status = msgJson.getIntValue("status")
            }
            if (msgJson.containsKey("loadStatus")) {
                message.loadStatus = msgJson.getString("loadStatus")
            }
            privateChatMessageDao.insertMessage(message)
        }
    }

    // 辅助方法：保存群聊消息
    private fun saveGroupMessages(messagesArray: JSONArray, conversationId: Long) {
        for (j in messagesArray.indices) {
            val msgJson = messagesArray.getJSONObject(j)
            val message: GroupChatMessage = GroupChatMessage()
            message.conversationId = conversationId
            // 处理可能缺失的字段
            if (msgJson.containsKey("messageId")) {
                message.messageId = msgJson.getString("messageId")
            }

            if (msgJson.containsKey("tmpId")) {
                message.tmpId = msgJson.getString("tmpId")
            }

            if (msgJson.containsKey("sendId")) {
                message.sendId = msgJson.getLong("sendId")
            }

            if (msgJson.containsKey("groupId")) {
                message.groupId = msgJson.getLong("groupId")
            }

            if (msgJson.containsKey("content")) {
                message.content = msgJson.getString("content")
            }

            if (msgJson.containsKey("sendTime")) {
                message.sendTime = msgJson.getLong("sendTime")
            }

            if (msgJson.containsKey("selfSend")) {
                message.isSelfSend = msgJson.getBooleanValue("selfSend")
            }

            if (msgJson.containsKey("type")) {
                message.type = msgJson.getIntValue("type")
            }

            if (msgJson.containsKey("status")) {
                message.status = msgJson.getIntValue("status")
            }

            if (msgJson.containsKey("readedCount")) {
                message.readedCount = msgJson.getIntValue("readedCount")
            }

            if (msgJson.containsKey("loadStatus")) {
                message.loadStatus = msgJson.getString("loadStatus")
            }

            if (msgJson.containsKey("sendNickName")) {
                message.sendNickName = msgJson.getString("sendNickName")
            }


            // 处理 @用户ID
            if (msgJson.containsKey("atUserIds")) {
                val atUserIds = msgJson.getJSONArray("atUserIds")
                val atUserIdsStr = StringBuilder()
                for (k in atUserIds.indices) {
                    if (k > 0) {
                        atUserIdsStr.append(",")
                    }
                    atUserIdsStr.append(atUserIds.getLong(k))
                }
                message.atUserIds = atUserIdsStr.toString()
            }

            if (msgJson.containsKey("receipt")) {
                message.isReceipt = msgJson.getBooleanValue("receipt")
            }

            if (msgJson.containsKey("receiptOk")) {
                message.isReceiptOk = msgJson.getBooleanValue("receiptOk")
            }
            groupChatMessageDao.insertMessage(message)
        }
    }

    /**
     * 保存聊天索引信息
     */
    fun saveChatIndex(chatsDataJson: String?, userId: Long) {
        try {
            MMKV.defaultMMKV().encode("chats-app-" + userId,chatsDataJson)
        } catch (e: java.lang.Exception) {
            Log.e("ChatStorageManager", "Error saving chat index: " + e.message)
        }
    }

    /**
     * 加载聊天索引信息
     */
    fun loadChatIndex(userId: Long): String? {
        return MMKV.defaultMMKV().decodeString("chats-app-" + userId)
    }

    /**
     * 初始时，获取所有会话
     * 加载前20条消息
     */
//    fun getAllChats(userId: Long): JSONObject {
//        try {
//            // 获取元数据
//            var metadata: ChatMetadata? = metadataDao.getMetadata(userId)
//            if (metadata == null) {
//                metadata = ChatMetadata()
//                metadata.setUserId(userId)
//                metadata.setPrivateMsgMaxId(0)
//                metadata.setGroupMsgMaxId(0)
//                metadata.setLastUpdateTime(System.currentTimeMillis())
//            }
//            // 获取所有会话
//            val conversations: MutableList<ChatConversation> =
//                chatConversationDao.getAllConversations(userId)
//            // 为每个会话加载消息
//            for (conversation in conversations) {
//
//                // 根据会话类型加载不同类型的消息
//                if ("PRIVATE" == conversation.type) {
//                    val messages: MutableList<PrivateChatMessage?> =
//                        privateChatMessageDao.getMessagesForConversation(conversation.id, 0, 20)
//                    for (message in messages) {
//                        conversation.messages.add(message)
//                    }
//                } else if ("GROUP" == conversation.type) {
//                    val messages: MutableList<GroupChatMessage?> =
//                        groupChatMessageDao.getMessagesForConversation(conversation.id, 0, 20)
//                    for (message in messages) {
//                        conversation.messages.add(message)
//                    }
//                }
//            }
//
//            // 构建返回数据
//            val result = JSONObject()
//            result.put("chats", conversations)
//            result.put("privateMsgMaxId", metadata.getPrivateMsgMaxId())
//            result.put("groupMsgMaxId", metadata.getGroupMsgMaxId())
//            return result
//        } catch (e: Exception) {
//            Log.e("xxx", "Error in getAllChats", e)
//            return JSONObject()
//        }
//    }

    /**
     * 删除消息
     */
    fun deleteMessageByMessageId(messageId: String,type :String): Boolean {
        try {
            db.runInTransaction({
                if("PRIVATE" == type){
                    privateChatMessageDao.deleteMessageById(messageId)
                }else{
                    groupChatMessageDao.deleteMessageById(messageId)
                }
            })
            return true
        } catch (e: java.lang.Exception) {
            Log.e("xxx", "Error in deleteMessage", e)
            return false
        }
    }
    /**
     * 删除会话
     */
    fun deleteChats(userId: Long, chatsToDelete: JSONArray): Boolean {
        try {
            db.runInTransaction({
                for (i in chatsToDelete.indices) {
                    val chatJson = chatsToDelete.getJSONObject(i)
                    val type = chatJson.getString("type")
                    val targetId = chatJson.getLong("targetId")
                    // 标记会话为已删除
//                    chatConversationDao
//                        .markConversationAsDeleted(userId, targetId, type)
                }
            })
            return true
        } catch (e: java.lang.Exception) {
            Log.e("xxx", "Error in deleteChats", e)
            return false
        }
    }


    fun saveMetadata(userId: Long, metadata: JSONObject): Boolean {
        try {
            val chatMetadata = ChatMetadata()
            chatMetadata.userId = userId
            chatMetadata.privateMsgMaxId = metadata.getLong("privateMsgMaxId")
            chatMetadata.groupMsgMaxId = metadata.getLong("groupMsgMaxId")
            chatMetadata.lastUpdateTime = System.currentTimeMillis()
            metadataDao.insertMetadata(chatMetadata)
            return true
        } catch (e: java.lang.Exception) {
            Log.e("xxx", "Error in saveMetadata", e)
            return false
        }
    }

    fun hasData(userId: Long): Boolean {
        try {
            val metadata: ChatMetadata? = metadataDao.getMetadata(userId)
            return metadata != null
        } catch (e: java.lang.Exception) {
            Log.e("xxx", "Error checking if data exists", e)
            return false
        }
    }

    fun deleteAllChats() : Boolean{
        try {
            db.runInTransaction({
                metadataDao.deleteAll()
                chatConversationDao.deleteAll()
                privateChatMessageDao.deleteAllChats()
                groupChatMessageDao.deleteAllChats()
            })
            return true
        } catch (e: java.lang.Exception) {
            Log.e("xxx", "Error in deleteAllChats", e)
            return false
        }
    }

    fun deleteAllFriends() : Boolean{
        try {
            db.runInTransaction({
                friendsDao.deleteAllFriends()
            })
            return true
        } catch (e: java.lang.Exception) {
            Log.e("xxx", "Error in deleteAllFriends", e)
            return false
        }
    }

    fun jsonStringToMutableMap(jsonString: String): MutableMap<String?, Any?> {
        // 使用Fastjson解析为Map<String?, Any?>
        val map: Map<String?, Any?> = JSON.parseObject(jsonString, object : TypeReference<Map<String?, Any?>?>() {}.type)
        // 将不可变Map转换为可变Map
        return map.toMutableMap()

    }

    /**
     * 添加群
     */
    fun addGroup(group: Group) {
        groupDao.addGroup(group)
    }

    /**
     * 批量添加或更新群组
     */
    fun addGroups(groups: List<Group>) {
        groupDao.addGroups(groups)
    }

    /**
     * 获取群组信息
     */
    fun getAllGroups(): List<Group> {
        return groupDao.selectAllData()
    }

    /**
     * 更新群
     */
    fun updateGroup(group: Group) {
        groupDao.updateGroup(group)
    }
    /**
     * 删除群
     */
    fun deleteGroup(groupId: Long) {
        groupDao.deleteGroupById(groupId)
    }

    fun clearGroup(){
        groupDao.deleteGroup()
    }
//
//    /**
//     * 插入或更新群聊的发送者密钥
//     */
//    fun insertOrUpdateSenderKey(senderKeyEntity: SenderKeyEntity){
//        senderKeyDao.insertOrUpdate(senderKeyEntity)
//    }
//
//    fun getSenderKey(keyId: String): SenderKeyEntity? {
//        return senderKeyDao.getById(keyId)
//    }
}