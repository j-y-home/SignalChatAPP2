package com.example.endtoendencryptionsystem.utils


import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.PrivateChatMessage
import com.example.endtoendencryptionsystem.entiy.vo.FriendVO
import com.example.endtoendencryptionsystem.entiy.vo.PrivateMessageVO
import java.util.Date

fun FriendVO.toFriend(): Friend {
    return Friend(
        id = null, // 主键自动生成
        userId = this.userId.toInt(),
        friendId = this.id,
        friendNickName = this.nickName,
        friendHeadImage = this.headImage,
        createdTime = this.createdTime,
        preKeyBundleMaker = this.preKeyBundleMaker
    )
}
