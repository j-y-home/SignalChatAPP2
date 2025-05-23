package com.example.endtoendencryptionsystem.entiy.database.key

import androidx.room.Entity
import androidx.room.PrimaryKey

// 会话记录表
@Entity(tableName = "signal_sessions")  
data class SignalSession(  
    @PrimaryKey
    val sessionKey: String, // address + deviceId组合
    val userId: String,  
    val sessionRecord: String, // 序列化的SessionRecord  
    val lastUpdateTime: Long = System.currentTimeMillis()  
)  