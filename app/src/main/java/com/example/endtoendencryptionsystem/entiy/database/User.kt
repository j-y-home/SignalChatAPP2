package com.example.endtoendencryptionsystem.entiy.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonFormat
import kotlinx.parcelize.Parcelize
import java.util.Date


/**
 * 用户表
 */
@Entity(tableName = "im_user")
@Parcelize
class User(
    /**
     * id
     */
    @PrimaryKey
    var id:Long?,


    /**
     * 用户昵称
     */
    @ColumnInfo(name = "nick_name")
    var nickName: String,
    /**
     * 用户头像
     */
    @ColumnInfo(name = "head_image")
    var headImage: String,
    /**
     * 头像缩略图
     */
    @ColumnInfo(name = "head_image_thumb")
    var headImageThumb: String,

    /**
     * 密码
     */
    var password: String,
    /**
     * 性别 0:男 1::女
     */
    var sex: Int,
    /**
     * 个性签名
     */
    var signature: String?,
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
     * 最后登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ColumnInfo(name = "last_login_time")
    var lastLoginTime: Date?,


    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ColumnInfo(name = "created_time")
    var createdTime: Date = Date(),

    /**
     * 账号类型 1:普通用户 2:wx小程序审核账户
     */
    var type: Int,

) : Parcelable
