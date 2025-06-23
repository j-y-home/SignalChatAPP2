package com.example.endtoendencryptionsystem.adapter

import android.widget.TextView
import com.brioal.circlehead.CircleHead
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.endtoendencryptionsystem.R
import com.example.endtoendencryptionsystem.entiy.FriendItem
import com.example.endtoendencryptionsystem.widget.HeadImageView
import com.example.endtoendencryptionsystem.widget.TextImageView

class GroupedFriendAdapter(data: MutableList<FriendItem> = ArrayList()) :
    BaseMultiItemQuickAdapter<FriendItem, BaseViewHolder>(data) {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_FRIEND = 1
    }

    init {
        addItemType(TYPE_HEADER, R.layout.contact_header_item)
        addItemType(TYPE_FRIEND, R.layout.contact_item)
    }

    override fun convert(holder: BaseViewHolder, item: FriendItem) {
        when (holder.itemViewType) {
            TYPE_HEADER -> {
                val header = holder.getView<TextView>(R.id.header_title)
                header.text = (item as FriendItem.Header).letter
            }
            TYPE_FRIEND -> {
                val friend = (item as FriendItem.FriendEntry).friend
                val tvName = holder.getView<TextView>(R.id.contact_name)
                tvName.text = friend.friendNickName

                val ivTou = holder.getView<HeadImageView>(R.id.iv_tou)
                ivTou.setName(friend.friendNickName)
                ivTou.setUrl(friend.friendHeadImage)
            }
        }
    }
}

