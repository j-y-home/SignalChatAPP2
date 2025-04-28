package com.example.endtoendencryptionsystem.entiy.database

import android.graphics.ColorSpace.Model
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonFormat
import kotlinx.parcelize.Parcelize
import java.util.Date


/**
 * 群成员
 */
@Entity(tableName = "im_group_member")
@Parcelize
class GroupMember(
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
     * 用户id
     */
    @ColumnInfo(name = "user_id")
    var userId: Long?,

    /**
     * 用户昵称
     */
    @ColumnInfo(name = "user_nick_name")
    var userNickName: String,

    /**
     * 显示昵称备注
     */
    @ColumnInfo(name = "remark_nick_name")
    var remarkNickName: String,

    /**
     * 用户头像
     */
    @ColumnInfo(name = "head_image")
    var headImage: String,

    /**
     * 显示群名备注
     */
    @ColumnInfo(name = "remark_group_name")
    var remarkGroupName: String,

    /**
     * 是否已退出
     */
    var quit: Boolean,

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    var createdTime: Date = Date(),

    /**
     * 退出时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    var quitTime: Date?,

) : Parcelable {
    fun getShowNickName(): String {
        return remarkNickName ?: userNickName ?: ""
    }
}
