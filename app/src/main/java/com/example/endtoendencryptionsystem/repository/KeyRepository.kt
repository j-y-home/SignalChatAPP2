package com.example.endtoendencryptionsystem.repository

import android.app.Application
import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.TypeReference
import com.example.endtoendencryptionsystem.entiy.database.ChatConversation
import com.example.endtoendencryptionsystem.entiy.database.ChatMetadata
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.Group
import com.example.endtoendencryptionsystem.entiy.database.GroupChatMessage
import com.example.endtoendencryptionsystem.entiy.database.PrivateChatMessage
import com.example.endtoendencryptionsystem.entiy.database.PrivateMessage
import com.example.endtoendencryptionsystem.entiy.database.key.SignalIdentityKey
import com.example.endtoendencryptionsystem.entiy.database.key.SignalPreKey
import com.example.endtoendencryptionsystem.entiy.database.key.SignalSenderKey
import com.example.endtoendencryptionsystem.entiy.database.key.SignalSession
import com.example.endtoendencryptionsystem.entiy.database.key.SignalSignedPreKey



/**
 * 密钥相关的单独整理出来
 */
class KeyRepository(val app: Application) {

    private var db = AppDatabase.getDatabase(app)
    private val signalKeyDao = db.signalKeyDao()
    private val TAG: String = "KeyRepository"


    // Signal Protocol 密钥管理方法
    fun getIdentityKey(userId: String): SignalIdentityKey? {
        return signalKeyDao.getIdentityKey(userId)
    }

    fun saveIdentityKey(identityKey: SignalIdentityKey) {
        signalKeyDao.insertIdentityKey(identityKey)
    }

    fun getPreKey(keyId: Int, userId: String): SignalPreKey? {
        return signalKeyDao.getPreKey(keyId, userId)
    }

    fun savePreKey(preKey: SignalPreKey) {
        signalKeyDao.insertPreKey(preKey)
    }

    fun deletePreKey(keyId: Int, userId: String) {
        signalKeyDao.deletePreKey(keyId, userId)
    }

    fun getSignedPreKey(keyId: Int, userId: String): SignalSignedPreKey? {
        return signalKeyDao.getSignedPreKey(keyId, userId)
    }

    fun saveSignedPreKey(signedPreKey: SignalSignedPreKey) {
        signalKeyDao.insertSignedPreKey(signedPreKey)
    }

    fun getSession(sessionKey: String, userId: String): SignalSession? {
        return signalKeyDao.getSession(sessionKey, userId)
    }

    //getAllSessions
    fun getAllSessions(userId: String): List<SignalSession> {
        return signalKeyDao.getAllSessions(userId)
    }

    fun saveSession(session: SignalSession) {
        signalKeyDao.insertSession(session)
    }

    fun getSenderKey(senderKeyName: String, userId: String): SignalSenderKey? {
        return signalKeyDao.getSenderKey(senderKeyName, userId)
    }

    fun saveSenderKey(senderKey: SignalSenderKey) {
        Log.e("xxxx","走这儿？"+ JSON.toJSONString(senderKey))
        signalKeyDao.insertSenderKey(senderKey)
    }

    fun deleteSenderKey(senderKeyName: String){
        signalKeyDao.deleteSenderKey(senderKeyName)
    }

    fun deleteSenderKeysByGroupId(groupId:String,userId:String){
        signalKeyDao.deleteSenderKeysByGroupId(groupId,userId)
    }


    fun getFriendPreKeys(friendId: String): List<SignalPreKey> {
        return signalKeyDao.getFriendPreKeys(friendId)
    }

    fun getFriendSignedPreKeys(friendId: String): List<SignalSignedPreKey> {
        return signalKeyDao.getFriendSignedPreKeys(friendId)
    }

    fun saveFriendPreKey(preKey: SignalPreKey) {
        signalKeyDao.insertPreKey(preKey)
    }
    fun saveFriendSignedPreKey(signedPreKey: SignalSignedPreKey) {
        signalKeyDao.insertSignedPreKey(signedPreKey)
    }

    //getAllSignedPreKeys
    fun getAllSignedPreKeys(userId: String): List<SignalSignedPreKey> {
        return signalKeyDao.getAllSignedPreKeys(userId)
    }

    //deleteSignedPreKey
    fun deleteSignedPreKey(keyId: Int, userId: String) {
        signalKeyDao.deleteSignedPreKey(keyId, userId)
    }

    //deleteSession(sessionKey, userId)
    fun deleteSession(sessionKey: String, userId: String) {
        signalKeyDao.deleteSession(sessionKey, userId)
    }

    //deleteAllSessionsForName
    fun deleteAllSessionsForName(sessionKey: String, userId: String) {
        signalKeyDao.deleteAllSessionsForName(sessionKey, userId)
    }
}