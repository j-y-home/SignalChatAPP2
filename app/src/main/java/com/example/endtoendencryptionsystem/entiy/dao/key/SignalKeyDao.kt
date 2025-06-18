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
import io.reactivex.rxjava3.core.Flowable
import org.whispersystems.libsignal.groups.SenderKeyName

/**
 * 密钥相关的操作DAO
 */
@Dao
interface SignalKeyDao {  
    // --------------身份密钥操作----------------
    @Query("SELECT * FROM signal_identity_keys WHERE userId = :userId")  
    suspend fun getIdentityKey(userId: String): SignalIdentityKey
      
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdentityKey(identityKey: SignalIdentityKey)


      
    // --------------预密钥操作----------------
    @Query("SELECT * FROM signal_pre_keys WHERE keyId = :keyId AND userId = :userId")
    suspend fun getPreKey(keyId: Int, userId: String): SignalPreKey?
      
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreKey(preKey: SignalPreKey)
      
    @Query("DELETE FROM signal_pre_keys WHERE keyId = :keyId AND userId = :userId")
    suspend fun deletePreKey(keyId: Int, userId: String)


      
    // -----------------签名预密钥操作--------------------
    @Query("SELECT * FROM signal_signed_pre_keys WHERE keyId = :keyId AND userId = :userId")
    suspend fun getSignedPreKey(keyId: Int, userId: String): SignalSignedPreKey?
      
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignedPreKey(signedPreKey: SignalSignedPreKey)

    //getAllSignedPreKeys
    @Query("SELECT * FROM signal_signed_pre_keys WHERE userId = :userId")
    suspend fun getAllSignedPreKeys(userId: String): List<SignalSignedPreKey>

    //deleteSignedPreKey
    @Query("DELETE FROM signal_signed_pre_keys WHERE keyId = :keyId AND userId = :userId")
    suspend  fun deleteSignedPreKey(keyId: Int, userId: String)


      
    // -------------发送者密钥操作--------------
    @Query("SELECT * FROM signal_sender_keys WHERE senderKeyName = :senderKeyName AND userId = :userId")
    suspend fun getSenderKey(senderKeyName: String, userId: String): SignalSenderKey?
      
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSenderKey(senderKey: SignalSenderKey)

    @Query("DELETE FROM signal_sender_keys WHERE senderKeyName = :senderKeyName ")
    suspend  fun deleteSenderKey(senderKeyName: String)

    @Query("DELETE FROM signal_sender_keys WHERE senderKeyName LIKE :groupId || '%' AND userId = :userId")
    suspend  fun deleteSenderKeysByGroupId(groupId: String, userId: String)



    // -----------------会话操作-----------------------
    @Query("SELECT * FROM signal_sessions WHERE sessionKey = :sessionKey AND userId = :userId")
    suspend fun getSession(sessionKey: String, userId: String): SignalSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SignalSession)

    //getAllSessions(userId)
    @Query("SELECT * FROM signal_sessions WHERE userId = :userId")
    suspend fun getAllSessions(userId: String): List<SignalSession>

    //deleteSession(sessionKey, userId)
    @Query("DELETE FROM signal_sessions WHERE sessionKey = :sessionKey AND userId = :userId")
    suspend fun deleteSession(sessionKey: String, userId: String)

    //deleteAllSessionsForName
    @Query("DELETE FROM signal_sessions WHERE sessionKey LIKE '%' || :name || '%' AND userId = :userId")
    suspend fun deleteAllSessionsForName(name: String, userId: String)




    // 获取好友的预密钥
    @Query("SELECT * FROM signal_pre_keys WHERE userId = :friendId")
    suspend  fun getFriendPreKeys(friendId: String): List<SignalPreKey>

    // 获取好友的签名预密钥
    @Query("SELECT * FROM signal_signed_pre_keys WHERE userId = :friendId")
    suspend fun getFriendSignedPreKeys(friendId: String): List<SignalSignedPreKey>

}