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
 * showName 的作用
 * showName 是会话对象的属性，用于在对话列表中显示会话的名称 chat-item.vue:11 。它的值根据会话类型来确定：
 *
 * 私聊会话: showName 显示对方的昵称
 * 群聊会话: showName 显示群组名称
 * 系统会自动更新 showName 以保持同步：
 *
 * 当好友昵称变化时，更新私聊会话的 showName chatStore.js:294-302
 * 当群组名称变化时，更新群聊会话的 showName chatStore.js:316-324
 * sendNickName 的作用
 * sendNickName 是消息对象的属性，用于记录发送该消息的用户昵称。它主要有两个用途：
 *
 * 会话列表预览: 在对话列表中显示最后一条消息的发送者名称 chat-item.vue:18
 *
 * 消息存储: 在插入新消息时，会将发送者昵称保存到会话的 sendNickName 字段 chatStore.js:186
 *
 * 显示逻辑区别
 * 在会话列表的 chat-item 组件中，有一个计算属性 isShowSendName 来决定是否显示发送者名称：
 *
 * 只有群聊的普通消息才会显示 sendNickName chat-item.vue:60-71
 * 私聊消息不显示发送者名称，因为只有两个人
 */
class ConverssationAdapter(data: MutableList<ChatConversation> = ArrayList()) :
    BaseQuickAdapter<ChatConversation, BaseViewHolder>(R.layout.item_chat_conv_layout,data) {

        override fun convert(holder: BaseViewHolder, item: ChatConversation) {
            var chatName = holder.getView<TextView>(R.id.chat_name)
            chatName.text = item.showName
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
            }
    }


}