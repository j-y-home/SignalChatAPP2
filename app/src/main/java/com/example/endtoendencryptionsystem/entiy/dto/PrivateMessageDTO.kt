package com.example.endtoendencryptionsystem.entiy.dto

/**
 * type: 消息类型 0:文字 1:图片 2:文件 3:语音 4:视频
 */
data class PrivateMessageDTO(val recvId: Long, val messageId: String, var content: String, val type: Int = 0)
