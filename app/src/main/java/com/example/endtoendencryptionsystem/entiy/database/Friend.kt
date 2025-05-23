package com.example.endtoendencryptionsystem.entiy.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.endtoendencryptionsystem.model.PreKeyBundleMaker
import com.example.endtoendencryptionsystem.model.StoreMaker
import com.fasterxml.jackson.annotation.JsonFormat
import kotlinx.parcelize.Parcelize
import java.util.Date


/**
 * 好友表
 * friendId不可重复
 */
@Entity(tableName = "im_friend",indices = [Index(value = ["friend_id"], unique = true)])
@Parcelize
class Friend(
    /**
     * id
     */
    @PrimaryKey(autoGenerate = true)
    var id:Int= -1,
    /**
     * 用户id
     */
    @ColumnInfo(name = "user_id")
    var userId: Long?,
    /**
     * 好友id
     */
    @ColumnInfo(name = "friend_id")
    var friendId: Long,
    /**
     * 用户昵称
     */
    @ColumnInfo(name = "friend_nick_name")
    var friendNickName: String?,
    /**
     * 用户头像
     */
    @ColumnInfo(name = "friend_head_image")
    var friendHeadImage: String?,

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ColumnInfo(name = "created_time")
    var createdTime: Date = Date(),

    @ColumnInfo(name = "registration_id")
    var registrationId:Int = 0,

    var identityKey:String = "",

    /**
     * 预密钥
     */
    @ColumnInfo(name="pre_key_bundle_maker")
    var preKeyBundleMaker: String?,


) : Parcelable
