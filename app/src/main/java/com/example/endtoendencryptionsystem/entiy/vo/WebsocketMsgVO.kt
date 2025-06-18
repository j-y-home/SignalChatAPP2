package com.example.endtoendencryptionsystem.entiy.vo

import java.util.Date

/**
 * websocket收到的消息
 */
data class WebsocketMsgVO (
    val id: Long? = null,
    val sendId: Long = 0,
    val recvId: Long = 0,
    //消息类型
    val type: Int,
    //消息体 该content可能是任意类型的，该如何写
    val content: String? =null,
    //发送时间
    val sendTime:Long

)
