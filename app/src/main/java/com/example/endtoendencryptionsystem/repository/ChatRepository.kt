package com.example.endtoendencryptionsystem.repository

import android.app.Application
import android.util.Log
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.example.endtoendencryptionsystem.entiy.database.ChatConversation
import com.example.endtoendencryptionsystem.entiy.database.ChatMetadata
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.GroupChatMessage
import com.example.endtoendencryptionsystem.entiy.database.PrivateChatMessage
import com.example.endtoendencryptionsystem.entiy.database.PrivateMessage
import com.example.endtoendencryptionsystem.utils.json
import com.example.endtoendencryptionsystem.utils.toObject


class ChatRepository(val app: Application) {

    private var db = AppDatabase.getDatabase(app)
    private val friendsDao = db.friendDao()
    private val privateMessageDao = db.privateMessageDao()
    private val metadataDao = db.metadataDao()
    private val chatConversationDao = db.chatConversationDao()
    private val privateChatMessageDao = db.privateChatMessageDao()
    private val groupChatMessageDao = db.groupChatMessageDao()

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
            if (msgJson.containsKey("id")) {
                message.serverMsgId = msgJson.getLong("id")
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
            if (msgJson.containsKey("id")) {
                message.setServerMsgId(msgJson.getLong("id"))
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
     * 获取所有会话
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
       //     val chatList: MutableList<ChatConversation> = ArrayList<ChatConversation>()

            // 为每个会话加载消息
            for (conversation in conversations) {
//                val chatJson = JSONObject()
//                chatJson.put("type", conversation.getType())
//                chatJson.put("targetId", conversation.getTargetId())
//                chatJson.put("showName", conversation.getShowName())
//                chatJson.put("headImage", conversation.getHeadImage())
//                chatJson.put("lastContent", conversation.getLastContent())
//                chatJson.put("lastSendTime", conversation.getLastSendTime())
//                chatJson.put("unreadCount", conversation.getUnreadCount())
//                chatJson.put("atMe", conversation.isAtMe())
//                chatJson.put("atAll", conversation.isAtAll())
//                chatJson.put("lastTimeTip", conversation.getLastTimeTip())
//                chatJson.put("sendNickName", conversation.getSendNickName())
//                chatJson.put("stored", true) // 标记为已存储
                conversation.isStored = true

                val messagesArray = JSONArray()

                // 根据会话类型加载不同类型的消息
                if ("PRIVATE" == conversation.getType()) {
                    val messages: MutableList<PrivateChatMessage?> =
                        privateChatMessageDao.getMessagesForConversation(conversation.getId(), 0, 3000)
                    for (message in messages) {
                        conversation.messages.add(message)
                    }
                } else if ("GROUP" == conversation.getType()) {
                    val messages: MutableList<GroupChatMessage?> =
                        groupChatMessageDao.getMessagesForConversation(conversation.getId(), 0, 3000)
                    for (message in messages) {
                        conversation.messages.add(message)
                    }
                }

            //    chatList.add(conversation)
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

}