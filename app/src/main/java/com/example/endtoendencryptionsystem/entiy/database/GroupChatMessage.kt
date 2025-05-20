package com.example.endtoendencryptionsystem.entiy.database

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "group_chat_message",
    foreignKeys = [ForeignKey(
        entity = ChatConversation::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("conversationId"),
        onDelete = ForeignKey.Companion.CASCADE
    )],
    indices = [Index("conversationId"), Index("sendTime")]
)
class GroupChatMessage {
    @JvmField
    @PrimaryKey
    var messageId: String = ""

    @JvmField
    var tmpId: String? = null
    @JvmField
    var conversationId: Long = 0
    @JvmField
    var sendId: Long = 0
    @JvmField
    var groupId: Long = 0
    @JvmField
    var sendNickName: String? = null
    @JvmField
    var content: String? = null
    @JvmField
    var sendTime: Long = 0
    var isSelfSend: Boolean = false
    @JvmField
    var type: Int = 0
    @JvmField
    var status: Int = 0
    @JvmField
    var readedCount: Int = 0
    @JvmField
    var loadStatus: String? = null
    @JvmField
    var atUserIds: String? = null
    var isReceipt: Boolean = false

    // Getters and setters
    var isReceiptOk: Boolean = false

    // ...  
}