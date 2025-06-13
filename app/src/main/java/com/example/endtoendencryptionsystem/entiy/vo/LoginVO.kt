package com.example.endtoendencryptionsystem.entiy.vo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
class LoginVO(
     val userId: Long = 0,
     val accessToken: String? = null,
     val accessTokenExpiresIn: Int? = null,
     val refreshToken: String? = null,
     val refreshTokenExpiresIn: Int? = null
): Parcelable
