package com.example.endtoendencryptionsystem.entiy.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonFormat
import java.util.Date


/**
 * 好友
 */
@Entity(tableName = "im_friend")
class Friend(
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
     * 群名字
     */
    var name: String?,

    /**
     * 用户id
     */
    @ColumnInfo(name = "user_id")
    var userId: Long?,
    /**
     * 好友id
     */
    @ColumnInfo(name = "friend_id")
    var friendId: Long?,


    /**
     * 用户昵称
     */
    @ColumnInfo(name = "friend_nick_name")
    var friendNickName: String,
    /**
     * 用户头像
     */
    @ColumnInfo(name = "friend_head_image")
    var friendHeadImage: String,

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ColumnInfo(name = "created_time")
    var createdTime: Date = Date(),

)
