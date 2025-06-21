package com.example.endtoendencryptionsystem.entiy.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.example.endtoendencryptionsystem.adapter.MessageAdapter
import com.example.endtoendencryptionsystem.enums.MessageType
import org.jetbrains.annotations.NotNull

@Entity(tableName = "private_chat_message")
class PrivateChatMessage : MultiItemEntity {
    @PrimaryKey
    var messageId: String = ""
    var tmpId: String? = null
    var conversationId: Long = 0
    var sendId: Long = 0
    var recvId: Long = 0
    var content: String? = null
    var sendTime: Long = 0
    var isSelfSend: Boolean = false
    var type: Int = 0
    var status: Int = 0
    var loadStatus: String? = null

    override val itemType: Int
        get() {
            when (type) {
                MessageType.TIP_TEXT.code, MessageType.TIP_TIME.code -> return MessageAdapter.MESSAGE_TYPE_SYSTEM

                MessageType.TEXT.code -> return if (this.isSelfSend) MessageAdapter.MESSAGE_TYPE_SENT_TEXT else MessageAdapter.MESSAGE_TYPE_RECV_TEXT

                MessageType.IMAGE.code -> return if (this.isSelfSend) MessageAdapter.MESSAGE_TYPE_SENT_IMAGE else MessageAdapter.MESSAGE_TYPE_RECV_IMAGE

                else ->                 // 可选：处理未知类型或抛异常
                    return -1 // 或抛出异常表示不支持的消息类型
            }
        }
}