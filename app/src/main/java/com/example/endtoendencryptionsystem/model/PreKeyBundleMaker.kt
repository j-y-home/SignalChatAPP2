package com.example.endtoendencryptionsystem.model

import androidx.room.PrimaryKey
import com.example.endtoendencryptionsystem.entiy.database.key.SignalPreKey
import com.example.endtoendencryptionsystem.entiy.database.key.SignalSignedPreKey

/**
 * 注册到User信息里的
 */
class PreKeyBundleMaker {
    var identityKey: String? = null
    var registrationId: Int = 0
    var signedPreKeys: List<FriendSignedPreKey>? = null

    var preKeys: List<FriendPreKey>? = null
}

class FriendPreKey(
    val keyId: Int,
    val publicKey: String
)

class FriendSignedPreKey(
    val keyId: Int,
    val publicKey: String,
    val signature: String,
    val timestamp: Long
)
