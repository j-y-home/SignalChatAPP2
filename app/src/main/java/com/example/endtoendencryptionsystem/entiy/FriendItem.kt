package com.example.endtoendencryptionsystem.entiy

import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.chad.library.adapter.base.entity.MultiItemEntity

sealed class FriendItem : MultiItemEntity {
    abstract override val itemType: Int

    data class Header(val letter: String) : FriendItem() {
        override val itemType: Int
            get() = TYPE_HEADER
    }

    data class FriendEntry(val friend: Friend) : FriendItem() {
        override val itemType: Int
            get() = TYPE_FRIEND
    }

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_FRIEND = 1
    }
}
