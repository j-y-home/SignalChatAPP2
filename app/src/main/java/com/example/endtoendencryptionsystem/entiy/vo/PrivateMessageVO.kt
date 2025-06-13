package com.example.endtoendencryptionsystem.entiy.vo

import java.util.Date

data class PrivateMessageVO(
    val messageId:String,
    val sendId:Long,
    val recvId:Long,
    var content:String,
    val type:Int,
    val status:Int,
    val sendTime:Date
)