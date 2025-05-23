package com.example.endtoendencryptionsystem.entiy.database.key

import androidx.room.Entity
import androidx.room.PrimaryKey

// 预密钥表
@Entity(tableName = "signal_pre_keys")  
data class SignalPreKey(  
    @PrimaryKey
    val keyId: Int,
    val userId: String,  
    val keyPair: String, // Base64编码的密钥对  
    val createdTime: Long = System.currentTimeMillis()  
)  