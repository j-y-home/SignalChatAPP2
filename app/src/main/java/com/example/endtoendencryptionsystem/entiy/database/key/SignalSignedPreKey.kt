package com.example.endtoendencryptionsystem.entiy.database.key

import androidx.room.Entity
import androidx.room.PrimaryKey

// 签名预密钥表
@Entity(tableName = "signal_signed_pre_keys")  
data class SignalSignedPreKey(  
    @PrimaryKey
    val keyId: Int,
    val userId: String,  
    val keyPair: String,  
    val signature: String,  
    val timestamp: Long,  
    val createdTime: Long = System.currentTimeMillis()  
)  