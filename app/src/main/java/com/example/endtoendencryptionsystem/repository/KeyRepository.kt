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
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers


/**
 * 密钥相关的单独整理出来
 */
class KeyRepository(val app: Application) {

    private var db = AppDatabase.getDatabase(app)
    private val signalKeyDao = db.signalKeyDao()
    private val TAG: String = "KeyRepository"


    // Signal Protocol 密钥管理方法
    suspend fun getIdentityKey(userId: String): SignalIdentityKey {
        return signalKeyDao.getIdentityKey(userId)
    }

    suspend fun saveIdentityKey(identityKey: SignalIdentityKey) {
        signalKeyDao.insertIdentityKey(identityKey)
    }

    suspend fun getPreKey(keyId: Int, userId: String): SignalPreKey? {
        return signalKeyDao.getPreKey(keyId, userId)
    }

    suspend  fun savePreKey(preKey: SignalPreKey) {
        signalKeyDao.insertPreKey(preKey)
    }

    suspend fun deletePreKey(keyId: Int, userId: String) {
        signalKeyDao.deletePreKey(keyId, userId)
    }

    suspend  fun getSignedPreKey(keyId: Int, userId: String): SignalSignedPreKey? {
        return signalKeyDao.getSignedPreKey(keyId, userId)
    }

    suspend  fun saveSignedPreKey(signedPreKey: SignalSignedPreKey) {
        signalKeyDao.insertSignedPreKey(signedPreKey)
    }

    suspend  fun getSession(sessionKey: String, userId: String): SignalSession? {
        return signalKeyDao.getSession(sessionKey, userId)
    }

    //getAllSessions
    suspend fun getAllSessions(userId: String): List<SignalSession> {
        return signalKeyDao.getAllSessions(userId)
    }

    suspend  fun saveSession(session: SignalSession) {
        signalKeyDao.insertSession(session)
    }

    suspend fun getSenderKey(senderKeyName: String, userId: String): SignalSenderKey? {
        return signalKeyDao.getSenderKey(senderKeyName, userId)
    }

    suspend  fun saveSenderKey(senderKey: SignalSenderKey) {
        Log.e("xxxx","走这儿？"+ JSON.toJSONString(senderKey))
        signalKeyDao.insertSenderKey(senderKey)
    }

    suspend fun deleteSenderKey(senderKeyName: String){
        signalKeyDao.deleteSenderKey(senderKeyName)
    }

    suspend fun deleteSenderKeysByGroupId(groupId:String,userId:String){
        signalKeyDao.deleteSenderKeysByGroupId(groupId,userId)
    }


    suspend fun getFriendPreKeys(friendId: String): List<SignalPreKey> {
        return signalKeyDao.getFriendPreKeys(friendId)
    }

    suspend  fun getFriendSignedPreKeys(friendId: String): List<SignalSignedPreKey> {
        return signalKeyDao.getFriendSignedPreKeys(friendId)
    }

    suspend  fun saveFriendPreKey(preKey: SignalPreKey) {
        signalKeyDao.insertPreKey(preKey)
    }
    suspend  fun saveFriendSignedPreKey(signedPreKey: SignalSignedPreKey) {
        signalKeyDao.insertSignedPreKey(signedPreKey)
    }

    //getAllSignedPreKeys
    suspend  fun getAllSignedPreKeys(userId: String): List<SignalSignedPreKey> {
        return signalKeyDao.getAllSignedPreKeys(userId)
    }

    //deleteSignedPreKey
    suspend fun deleteSignedPreKey(keyId: Int, userId: String) {
        signalKeyDao.deleteSignedPreKey(keyId, userId)
    }

    //deleteSession(sessionKey, userId)
    suspend fun deleteSession(sessionKey: String, userId: String) {
        signalKeyDao.deleteSession(sessionKey, userId)
    }

    //deleteAllSessionsForName
    suspend  fun deleteAllSessionsForName(sessionKey: String, userId: String) {
        signalKeyDao.deleteAllSessionsForName(sessionKey, userId)
    }
}