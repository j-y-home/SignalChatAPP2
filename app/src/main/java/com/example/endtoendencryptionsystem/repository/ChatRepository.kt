package com.example.endtoendencryptionsystem.repository

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.TypeReference
import com.example.endtoendencryptionsystem.entiy.database.ChatConversation
import com.example.endtoendencryptionsystem.entiy.database.ChatMetadata
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.GroupChatMessage
import com.example.endtoendencryptionsystem.entiy.database.PrivateChatMessage
import com.example.endtoendencryptionsystem.entiy.database.PrivateMessage
import com.example.endtoendencryptionsystem.enums.MessageStatus
import com.example.endtoendencryptionsystem.utils.json
import com.example.endtoendencryptionsystem.utils.toJSONString
import com.example.endtoendencryptionsystem.utils.toObject
import com.tencent.mmkv.MMKV


class ChatRepository(val app: Application) {

    private var db = AppDatabase.getDatabase(app)
    private val friendsDao = db.friendDao()
    private val privateMessageDao = db.privateMessageDao()
    private val metadataDao = db.metadataDao()
    private val chatConversationDao = db.chatConversationDao()
    private val privateChatMessageDao = db.privateChatMessageDao()
    private val groupChatMessageDao = db.groupChatMessageDao()
    private val TAG: String = "ChatRepository"
    /**
     * 获取当前用户的所有好友
     * id:当前用户的id
     */
    fun selectAllFriendsByUserId(userId: Int): List<Friend> {
        return friendsDao.selectAllFriendsByUserId(userId);
    }

    /**
     * 获取某个好友的详细信息
     * id:好友id
     */
    fun selectFriendsByFriendId(friendId: Long): Friend {
        return friendsDao.selectFriendsByFriendId(friendId);
    }

    fun addFriend(friend: Friend) {
        friendsDao.addFriend(friend)
    }

    fun selectAllData():List<Friend>{
        return friendsDao.selectAllData()
    }

    fun insertPrivateMessage(privateMessage: PrivateMessage) {
        privateMessageDao.insertMessage(privateMessage)
    }

    fun getAllMsgFromFriend(userId: Int,friendId: Int): List<PrivateMessage> {
        return privateMessageDao.getAllMsgFromFriend(userId,friendId)
    }

    /**
     * 最新的设计：只传入当前最新的消息，不传入整个会话。
     * 保存单条消息到数据库
     * @param messageJson 消息JSON字符串
     * @param chatInfo 会话基本信息JSON字符串
     * @param userId 当前用户ID
     */
    fun saveNewMessage(messageJson: String, chatInfoJson: String, userId: Long) {
        try {
            // 在事务中执行数据库操作
            db.runInTransaction({
                // 获取会话对象
                val conversation: ChatConversation = json.toObject(chatInfoJson)
                conversation.userId = userId
                // 保存会话，获取会话ID
                val conversationId: Long
                val existingConversation: ChatConversation? = chatConversationDao.getConversation(
                    userId, conversation.targetId, conversation.type)
                if (existingConversation != null) {
                    conversation.id = existingConversation.id
                    chatConversationDao.updateConversation(conversation)
                    conversationId = existingConversation.id
                } else {
                    conversationId = chatConversationDao.insertConversation(conversation)
                }
                // 保存单条消息
                saveOneMessage(messageJson, conversationId, conversation.type)
                // TODO 更新会话的最后消息信息
              //  updateConversationLastMessage(conversation, jsonStringToMutableMap(messageJson),userId)
              //  chatConversationDao.updateConversation(conversation)
            })
        } catch (e: java.lang.Exception) {
            Log.e("ChatStorageManager", "Error saving message: " + e.message)
        }
    }


    /**
     * 保存单条消息到数据库
     * @param messageMap 消息数据Map
     * @param conversationId 会话ID
     * @param type 会话类型 ("PRIVATE" 或 "GROUP")
     */
    private fun saveOneMessage(messageJson: String, conversationId: Long, type: String?) {
        try {
            // 根据会话类型选择不同的DAO
            if ("PRIVATE" == type) {
                var message = json.toObject<PrivateChatMessage>(messageJson)
                // 跳过时间提示消息等特殊消息
                if(message.type == 20){
                    return
                }
                message.conversationId = conversationId
                var existingMessage = privateChatMessageDao.getMessagesById(message.messageId)
                Log.e("ChatStorageManager", "接收的消息: "+json.toJSONString(message))
                if (existingMessage != null) {
                    privateChatMessageDao.updateMessage(message)
                    Log.d("ChatStorageManager", "Updated existing private message: " + existingMessage.messageId)
                } else {

                    val insertedId: Long = privateChatMessageDao.insertMessage(message)
                    Log.d("ChatStorageManager", "Inserted new private message with ID: " + insertedId)
                }
            } else if ("GROUP" == type) {//TODO 待定
//                var message = json.toObject<GroupChatMessage>(messageJson)
//                // 跳过时间提示消息等特殊消息
//                if(existingMessage.type == 20){
//                    return
//                }
//                val groupMessageDao: GroupMessageDao = database.groupMessageDao()
//
//
//                // 检查消息是否已存在
//                val tmpId = messageMap.get("tmpId") as String?
//                val idDouble = messageMap.get("id") as Double?
//                val messageId = if (idDouble != null) idDouble.toLong() else 0
//
//                var existingMessage: GroupMessageEntity? = null
//                if (messageId > 0) {
//                    existingMessage = groupMessageDao.findById(messageId)
//                } else if (tmpId != null && !tmpId.isEmpty()) {
//                    existingMessage = groupMessageDao.findByTmpId(tmpId)
//                }
//
//                if (existingMessage != null) {
//                    // 更新现有消息
//                    updateGroupMessage(existingMessage, messageMap)
//                    groupMessageDao.update(existingMessage)
//                    Log.d(
//                        "ChatStorageManager",
//                        "Updated existing group message: " + existingMessage.getId()
//                    )
//                } else {
//                    // 创建新消息
//                    val newMessage: GroupMessageEntity? =
//                        createGroupMessage(messageMap, conversationId)
//                    val insertedId: Long = groupMessageDao.insert(newMessage)
//                    Log.d("ChatStorageManager", "Inserted new group message with ID: " + insertedId)
//                }
            }
        } catch (e: java.lang.Exception) {
            Log.e("ChatStorageManager", "Error saving message: " + e.message, e)
        }
    }

    /**
     * TODO 后期加
     * 更新消息的已读未读状态
     */
    private fun updateMessageStatus(messageJson: String) {

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
                        privateChatMessageDao.getMessagesForConversation(conversation.getId(), offset,limit)
                for (msg in privateMessages) {
                    messages.add(msg)
                }
            } else if ("GROUP" == type) {
                // 类似处理群聊消息...
                val groupChatMessages: MutableList<GroupChatMessage?> =
                    groupChatMessageDao.getMessagesForConversation(conversation.getId(), offset,limit)
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
                        userId, conversation.getTargetId(), conversation.getType())

                if (existingConversation != null) {
                    conversation.setId(existingConversation.getId())
                    chatConversationDao.updateConversation(conversation)
                    conversationId = existingConversation.getId()
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
                    if ("PRIVATE" == conversation.getType()) {
                        savePrivateMessages(messagesArray, conversationId)
                    } else if ("GROUP" == conversation.getType()) {
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
            message.setConversationId(conversationId)
            // 处理可能缺失的字段
            if (msgJson.containsKey("messageId")) {
                message.setMessageId(msgJson.getString("messageId"))
            }

            if (msgJson.containsKey("tmpId")) {
                message.setTmpId(msgJson.getString("tmpId"))
            }

            if (msgJson.containsKey("sendId")) {
                message.setSendId(msgJson.getLong("sendId"))
            }

            if (msgJson.containsKey("groupId")) {
                message.setGroupId(msgJson.getLong("groupId"))
            }

            if (msgJson.containsKey("content")) {
                message.setContent(msgJson.getString("content"))
            }

            if (msgJson.containsKey("sendTime")) {
                message.setSendTime(msgJson.getLong("sendTime"))
            }

            if (msgJson.containsKey("selfSend")) {
                message.setSelfSend(msgJson.getBooleanValue("selfSend"))
            }

            if (msgJson.containsKey("type")) {
                message.setType(msgJson.getIntValue("type"))
            }

            if (msgJson.containsKey("status")) {
                message.setStatus(msgJson.getIntValue("status"))
            }

            if (msgJson.containsKey("readedCount")) {
                message.setReadedCount(msgJson.getIntValue("readedCount"))
            }

            if (msgJson.containsKey("loadStatus")) {
                message.setLoadStatus(msgJson.getString("loadStatus"))
            }

            if (msgJson.containsKey("sendNickName")) {
                message.setSendNickName(msgJson.getString("sendNickName"))
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
                message.setAtUserIds(atUserIdsStr.toString())
            }

            if (msgJson.containsKey("receipt")) {
                message.setReceipt(msgJson.getBooleanValue("receipt"))
            }

            if (msgJson.containsKey("receiptOk")) {
                message.setReceiptOk(msgJson.getBooleanValue("receiptOk"))
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
    fun getAllChats(userId: Long): JSONObject {
        try {
            // 获取元数据
            var metadata: ChatMetadata? = metadataDao.getMetadata(userId)
            if (metadata == null) {
                metadata = ChatMetadata()
                metadata.setUserId(userId)
                metadata.setPrivateMsgMaxId(0)
                metadata.setGroupMsgMaxId(0)
                metadata.setLastUpdateTime(System.currentTimeMillis())
            }
            // 获取所有会话
            val conversations: MutableList<ChatConversation> =
                chatConversationDao.getAllConversations(userId)
            // 为每个会话加载消息
            for (conversation in conversations) {
                conversation.isStored = true
                // 根据会话类型加载不同类型的消息
                if ("PRIVATE" == conversation.getType()) {
                    val messages: MutableList<PrivateChatMessage?> =
                        privateChatMessageDao.getMessagesForConversation(conversation.getId(), 0, 20)
                    for (message in messages) {
                        conversation.messages.add(message)
                    }
                } else if ("GROUP" == conversation.getType()) {
                    val messages: MutableList<GroupChatMessage?> =
                        groupChatMessageDao.getMessagesForConversation(conversation.getId(), 0, 20)
                    for (message in messages) {
                        conversation.messages.add(message)
                    }
                }
            }

            // 构建返回数据
            val result = JSONObject()
            result.put("chats", conversations)
            result.put("privateMsgMaxId", metadata.getPrivateMsgMaxId())
            result.put("groupMsgMaxId", metadata.getGroupMsgMaxId())
            return result
        } catch (e: Exception) {
            Log.e("xxx", "Error in getAllChats", e)
            return JSONObject()
        }
    }

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
                    chatConversationDao
                        .markConversationAsDeleted(userId, targetId, type)
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

}