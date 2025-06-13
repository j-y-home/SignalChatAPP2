package com.example.endtoendencryptionsystem.entiy.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.fasterxml.jackson.annotation.JsonFormat
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Date


/**
 * 私聊消息
 */
@Entity(tableName = "im_private_message")
@Parcelize
class PrivateMessage (
    /**
     * id
     */
    @PrimaryKey(autoGenerate = true)
    var id:Long? = null,
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
     * TODO 后期完善
     * 消息类型 MessageType  0:文字 1:图片 2:文件 3:语音 4:视频 21:提示
     */
    var type: Int? = 0 ,

    /**
     * TODO 后期完善
     * 状态 0:未读 1:已读 2:撤回 3:已读
     */
    var status: Int? = 1 ,

    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    var sendTime: Date?,

) : Parcelable, MultiItemEntity{
    @Ignore
    override var itemType: Int = 0
}
