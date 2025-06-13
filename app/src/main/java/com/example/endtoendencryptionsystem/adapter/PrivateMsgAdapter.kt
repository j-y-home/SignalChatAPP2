package com.example.endtoendencryptionsystem.adapter

import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.endtoendencryptionsystem.R
import com.example.endtoendencryptionsystem.entiy.database.PrivateChatMessage
import com.example.endtoendencryptionsystem.entiy.database.PrivateMessage

/**
 * 私聊adapter
 */
class PrivateMsgAdapter(data: MutableList<PrivateChatMessage> = ArrayList()) :
    BaseMultiItemQuickAdapter<PrivateChatMessage, BaseViewHolder>(data) {

    companion object {
        const val TYPE_LEFT = 0
        const val TYPE_RIGHT = 1
    }

    init {
        addItemType(TYPE_LEFT, R.layout.item_chat_msg_private_left)
        addItemType(TYPE_RIGHT, R.layout.item_chat_msg_private_right)
    }

    override fun convert(holder: BaseViewHolder, item: PrivateChatMessage) {
        when (holder.itemViewType) {
            TYPE_LEFT -> {
                val ivTou = holder.getView<ImageView>(R.id.iv_tou)
                val tvMsg = holder.getView<TextView>(R.id.message_text)
//                if(friendHeaderImage.isNotEmpty()){
//                    Glide.with(context).load(friendHeaderImage).transform(CenterCrop(), RoundedCorners(24)).into(ivTou)
//                }
                tvMsg.text = item.content
            }
            TYPE_RIGHT -> {
                val ivTou = holder.getView<ImageView>(R.id.iv_tou)
                val tvMsg = holder.getView<TextView>(R.id.message_text)
                tvMsg.text = item.content
            }
        }
    }
}

