package com.example.endtoendencryptionsystem.entiy.vo

import java.util.Date


data class OnlineTerminalVO (
    val userId: Long = 0,
    //在线终端类型
    val terminals: List<Int>
)
