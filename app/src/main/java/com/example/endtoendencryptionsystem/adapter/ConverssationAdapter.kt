package com.example.endtoendencryptionsystem.adapter

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.endtoendencryptionsystem.R
import com.example.endtoendencryptionsystem.entiy.database.ChatConversation
import com.example.endtoendencryptionsystem.utils.TimeUtils


/**
 * 对话adapter
 */
class ConverssationAdapter(data: MutableList<ChatConversation> = ArrayList()) :
    BaseQuickAdapter<ChatConversation, BaseViewHolder>(R.layout.item_chat_conv_layout,data) {

        override fun convert(holder: BaseViewHolder, item: ChatConversation) {
            var chatName = holder.getView<TextView>(R.id.chat_name)
            chatName.text = item.sendNickName
            var sendTime = holder.getView<TextView>(R.id.chat_time)
            sendTime.text = TimeUtils.formatTimestamp(item.lastSendTime)

            var content = holder.getView<TextView>(R.id.chat_content_text)
            content.text = item.lastContent
            var chatNick = holder.getView<TextView>(R.id.chat_send_name)
            chatNick.text = item.sendNickName

            if(item.type == "PRIVATE"){//私聊
                chatNick.visibility = View.GONE
            }else{
                chatNick.visibility = View.VISIBLE
                chatName.text = item.showName
            }
    }


}