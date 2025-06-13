package com.example.endtoendencryptionsystem.entiy.database.key

import androidx.room.Entity
import androidx.room.PrimaryKey

// 发送者密钥表（群聊用）
@Entity(tableName = "signal_sender_keys")  
data class SignalSenderKey(  
    @PrimaryKey
    val senderKeyName: String, // groupId + address组合
    val userId: String,
    val groupId:String,
    val senderKeyRecord: String, // 序列化的SenderKeyRecord  
    val createdTime: Long = System.currentTimeMillis()  
)