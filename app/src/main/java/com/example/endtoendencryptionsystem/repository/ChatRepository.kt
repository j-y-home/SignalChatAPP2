package com.example.endtoendencryptionsystem.repository

import android.app.Application
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.PrivateMessage

class ChatRepository(val app: Application) {

    private var db = AppDatabase.getDatabase(app)
    private val friendsDao = db.friendDao()
    private val privateMessageDao = db.privateMessageDao()

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
}