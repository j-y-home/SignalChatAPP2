package com.example.endtoendencryptionsystem.entiy.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sender_keys")
data class SenderKeyEntity(  
    @PrimaryKey  
    val keyId: String,  // 格式: groupId::senderId  
    val serializedRecord: ByteArray,  // 序列化的SenderKeyRecord  
    val createdAt: Long,  
    val updatedAt: Long  
)  