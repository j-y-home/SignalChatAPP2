package com.example.endtoendencryptionsystem.entiy.database

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chat_conversation")
class ChatConversation {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var userId: Long=0
    var targetId: Long =0
    var type: String = ""
    var showName: String = ""
    var headImage: String = ""
    var lastContent: String = ""
    var lastSendTime: Long = 0
    var unreadCount: Int = 0
    var isAtMe: Boolean = false
    var isAtAll: Boolean = false
    var lastTimeTip: Long = 0
    var sendNickName: String = ""
}
