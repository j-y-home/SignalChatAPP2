package com.example.endtoendencryptionsystem.entiy.dao.key

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.endtoendencryptionsystem.entiy.database.key.SignalIdentityKey
import com.example.endtoendencryptionsystem.entiy.database.key.SignalPreKey
import com.example.endtoendencryptionsystem.entiy.database.key.SignalSenderKey
import com.example.endtoendencryptionsystem.entiy.database.key.SignalSession
import com.example.endtoendencryptionsystem.entiy.database.key.SignalSignedPreKey

/**
 * 密钥相关的操作DAO
 */
@Dao
interface SignalKeyDao {  
    // 身份密钥操作  
    @Query("SELECT * FROM signal_identity_keys WHERE userId = :userId")  
    fun getIdentityKey(userId: String): SignalIdentityKey?
      
    @Insert(onConflict = OnConflictStrategy.REPLACE)  
    fun insertIdentityKey(identityKey: SignalIdentityKey)
      
    // 预密钥操作  
    @Query("SELECT * FROM signal_pre_keys WHERE keyId = :keyId AND userId = :userId")  
    fun getPreKey(keyId: Int, userId: String): SignalPreKey?
      
    @Insert(onConflict = OnConflictStrategy.REPLACE)  
    fun insertPreKey(preKey: SignalPreKey)
      
    @Query("DELETE FROM signal_pre_keys WHERE keyId = :keyId AND userId = :userId")
    fun deletePreKey(keyId: Int, userId: String)
      
    // 签名预密钥操作  
    @Query("SELECT * FROM signal_signed_pre_keys WHERE keyId = :keyId AND userId = :userId")  
    fun getSignedPreKey(keyId: Int, userId: String): SignalSignedPreKey?
      
    @Insert(onConflict = OnConflictStrategy.REPLACE)  
    fun insertSignedPreKey(signedPreKey: SignalSignedPreKey)
      
    // 会话操作  
    @Query("SELECT * FROM signal_sessions WHERE sessionKey = :sessionKey AND userId = :userId")  
    fun getSession(sessionKey: String, userId: String): SignalSession?
      
    @Insert(onConflict = OnConflictStrategy.REPLACE)  
    fun insertSession(session: SignalSession)
      
    // 发送者密钥操作  
    @Query("SELECT * FROM signal_sender_keys WHERE senderKeyName = :senderKeyName AND userId = :userId")  
    fun getSenderKey(senderKeyName: String, userId: String): SignalSenderKey?
      
    @Insert(onConflict = OnConflictStrategy.REPLACE)  
    fun insertSenderKey(senderKey: SignalSenderKey)



    // 获取好友的预密钥
    @Query("SELECT * FROM signal_pre_keys WHERE userId = :friendId")
    fun getFriendPreKeys(friendId: String): List<SignalPreKey>

    // 获取好友的签名预密钥
    @Query("SELECT * FROM signal_signed_pre_keys WHERE userId = :friendId")
    fun getFriendSignedPreKeys(friendId: String): List<SignalSignedPreKey>
}