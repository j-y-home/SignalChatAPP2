package com.example.endtoendencryptionsystem.adapter

import androidx.appcompat.widget.LinearLayoutCompat
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.endtoendencryptionsystem.R
import com.example.endtoendencryptionsystem.entiy.database.ChatConversation


/**
 * 对话adapter
 */
class ConverssationAdapter(data: MutableList<ChatConversation> = ArrayList()) :
    BaseQuickAdapter<ChatConversation, BaseViewHolder>(R.layout.item_chat_conv_layout,data) {

        override fun convert(
        holder: BaseViewHolder,
        item: ChatConversation
    ) {

    }


}