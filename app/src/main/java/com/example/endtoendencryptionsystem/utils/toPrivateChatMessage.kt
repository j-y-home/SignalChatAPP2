package com.example.endtoendencryptionsystem.utils


import com.example.endtoendencryptionsystem.entiy.database.PrivateChatMessage
import com.example.endtoendencryptionsystem.entiy.vo.PrivateMessageVO

fun PrivateMessageVO.toPrivateChatMessage(self: Boolean, conId: Long): PrivateChatMessage {
    return PrivateChatMessage().apply {
        messageId = this@toPrivateChatMessage.messageId
        sendId = this@toPrivateChatMessage.sendId
        recvId = this@toPrivateChatMessage.recvId
        content = this@toPrivateChatMessage.content
        type = this@toPrivateChatMessage.type
        status = this@toPrivateChatMessage.status!!
        sendTime = this@toPrivateChatMessage.sendTime.time
        isSelfSend = self
        conversationId = conId
    }
}
