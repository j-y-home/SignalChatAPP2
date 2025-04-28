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
@Entity(tableName = "im_group_message")
@Parcelize
class GroupMessage(
    /**
     * id
     */
    @PrimaryKey
    var id:Long?,
    /**
     * 群id
     */
    @ColumnInfo(name = "group_id")
    var groupId: Long?,
    /**
     * 发送用户id
     */
    @ColumnInfo(name = "send_id")
    var sendId: Long?,
    /**
     * 发送用户昵称
     */
    @ColumnInfo(name = "send_nick_name")
    var sendNickName: String?,

    /**
     * 接收用户id，为空表示全体发送
     */
    @ColumnInfo(name = "recv_ids")
    var recvId: String?,
    /**
     *  @用户列表
     */
    var atUserIds: String?,

    /**
     * 发送内容
     */
    var content: String?,

    /**
     * 消息类型 MessageType
     */
    var type: Int?,

    /**
     * 是否回执消息
     */
    var receipt: Boolean?,

    /**
     * 是否回执成功
     */
    var receiptOK: Boolean?,

    /**
     * 状态
     */
    var status: Int? ,

    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    var sendTime: Date?,

) : Parcelable
