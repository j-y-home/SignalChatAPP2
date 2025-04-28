package com.example.endtoendencryptionsystem.entiy.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonFormat
import kotlinx.parcelize.Parcelize
import java.util.Date


/**
 * 私聊消息
 */
@Entity(tableName = "im_private_message")
@Parcelize
class PrivateMessage(
    /**
     * id
     */
    @PrimaryKey
    var id:Long?,
    /**
     * 发送用户id
     */
    @ColumnInfo(name = "send_id")
    var sendId: Long?,

    /**
     * 接收用户id
     */
    @ColumnInfo(name = "recv_id")
    var recvId: Long?,

    /**
     * 发送内容
     */
    var content: String?,

    /**
     * 消息类型 MessageType
     */
    var type: Int? ,

    /**
     * 状态
     */
    var status: Int? ,

    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    var sendTime: Date?,
    //添加无参构造函数

) : Parcelable {
    // 添加无参构造函数
    constructor() : this(null, null, null, null, null, null, null)
}
