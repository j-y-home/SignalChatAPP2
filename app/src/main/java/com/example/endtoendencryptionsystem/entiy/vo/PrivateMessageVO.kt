package com.example.endtoendencryptionsystem.entiy.vo

import java.util.Date

data class PrivateMessageVO(
    val id: Long? = 0,
    val messageId:String? = id.toString(),
    val sendId:Long,
    val recvId:Long,
    var content:String?,
    val type:Int,
    val status:Int?=-1,
    val sendTime:Date
)