package com.example.endtoendencryptionsystem.entiy.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonFormat
import java.util.Date


/**
 * 群
 */
@Entity(tableName = "im_group")
class Group(
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
     * 群主id
     */
    @ColumnInfo(name = "owner_id")
    var ownerId: Long?,

    /**
     * 群头像
     */
    @ColumnInfo(name = "head_image")
    var headImage: String?,

    /**
     * 群头像缩略图
     */
    @ColumnInfo(name = "head_image_thumb")
    var headImageThumb: String,

    /**
     * 群公告
     */
    var notice: String?,

    /**
     * 是否被封禁
     */
    @ColumnInfo(name = "is_banned")
    var isBanned: Boolean,

    /**
     * 被封禁原因
     */
    var reason: String?,

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ColumnInfo(name = "created_time")
    var createdTime: Date = Date(),

    /**
     * 是否已删除
     */
    var dissolve: Boolean,

)
