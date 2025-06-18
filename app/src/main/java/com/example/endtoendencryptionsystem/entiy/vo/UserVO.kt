package com.example.endtoendencryptionsystem.entiy.vo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
class UserVO(
     val id: Long = 0,
     val userName: String? = "",
     val nickName: String? = "",
     val sex: Int? = null,
     val type: Int? = 1,
     val signature:String? = "",
     val headImage:String? = "",
     val headImageThumb:String? = "",
     val online: Boolean? = false,
     val isBanned: Boolean? = false,
     val reason:String? = "",
     val preKeyBundleMaker:String? = ""
): Parcelable
