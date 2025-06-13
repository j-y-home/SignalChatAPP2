package com.example.endtoendencryptionsystem.entiy.dto

/**
 * type: 消息类型 0:文字 1:图片 2:文件 3:语音 4:视频
 */
data class GroupMessageDTO(val groupId: Long, val messageId: String, val content: String, val type: Int = 0,
                           val receipt: Boolean = false, val atUserIds:List<Long>)
