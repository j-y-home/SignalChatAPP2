package com.example.endtoendencryptionsystem.entiy.vo

import java.util.Date


data class FriendVO (
    //好友id
    val id: Long,
    val userId: Long = 0,
    //好友昵称
    val nickName: String,
    //好友头像
    val headImage: String = "",
    //公钥相关
    val preKeyBundleMaker: String = "",
    val createdTime:Date = Date()

)
