package com.example.endtoendencryptionsystem.adapter

import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.endtoendencryptionsystem.R
import com.example.endtoendencryptionsystem.entiy.database.Friend




/**
 *
 */
class FriendAdapter(data: MutableList<Friend> = ArrayList()) :
    BaseQuickAdapter<Friend, BaseViewHolder>(R.layout.contact_item,data) {

        override fun convert(holder: BaseViewHolder, item: Friend) {
            var tvName = holder.getView<TextView>(R.id.contact_name)

            tvName.text = item.friendNickName

        }


}