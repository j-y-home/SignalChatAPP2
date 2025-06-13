package com.example.endtoendencryptionsystem.entiy.vo

import java.util.Date

data class GroupMessageVO(
    val messageId:String,
    val groupId:Long,
    val sendId:Long,
    val sendNickName:String,
    val content:String,
    val receipt: Boolean,
    val receiptOk: Boolean,
    val type:Int,
    val readedCount:Int = 0,
    val atUserIds:List<Long>,
    val status:Int,
    val sendTime:Date
)