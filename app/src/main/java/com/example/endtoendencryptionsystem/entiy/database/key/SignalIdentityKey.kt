package com.example.endtoendencryptionsystem.entiy.database.key

import androidx.room.Entity
import androidx.room.PrimaryKey

// 身份密钥表
@Entity(tableName = "signal_identity_keys")  
data class SignalIdentityKey(  
    @PrimaryKey
    val userId: String,
    val identityKeyPair: String, // Base64编码的密钥对  
    val registrationId: Int,  
    val createdTime: Long = System.currentTimeMillis()  
)