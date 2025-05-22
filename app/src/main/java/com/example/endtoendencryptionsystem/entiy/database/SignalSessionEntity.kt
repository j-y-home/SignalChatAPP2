package com.example.endtoendencryptionsystem.entiy.database

import androidx.room.Entity

@Entity(tableName = "signal_sessions", primaryKeys = ["address", "deviceId"])
data class SignalSessionEntity(  
    val address: String,  
    val deviceId: Int,  
    val sessionData: ByteArray  
)