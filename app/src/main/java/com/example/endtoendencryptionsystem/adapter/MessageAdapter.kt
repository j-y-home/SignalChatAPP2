package com.example.endtoendencryptionsystem.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter

import com.example.endtoendencryptionsystem.ui.*
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.ControllerListener
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.ImageInfo
import com.alibaba.fastjson.JSON
import com.brioal.circlehead.CircleHead
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.endtoendencryptionsystem.R
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.PrivateChatMessage
import com.example.endtoendencryptionsystem.enums.MessageStatus
import com.example.endtoendencryptionsystem.enums.MessageType
import com.example.endtoendencryptionsystem.utils.TimestampUtil
import com.example.endtoendencryptionsystem.widget.HeadImageView
import com.example.endtoendencryptionsystem.widget.TextImageView
import com.tencent.mmkv.MMKV
import java.io.File
import java.util.*

class MessageAdapter(data: MutableList<PrivateChatMessage>,friend: Friend) :
    BaseMultiItemQuickAdapter<PrivateChatMessage, BaseViewHolder>(data) {
    private val mFriend = friend

    /**
     * 如果PrivateChatMessage的type为MessageType.TIP_TEXT或者MessageType.TiME_Text，则itemType是MESSAGE_TYPE_SYSTEM；
     * 如果PrivateChatMessage的type为MessageType.TEXT，且isSelfSend为true,则itemType是MESSAGE_TYPE_SENT_TEXT,isSelfSend为false,则itemType是MESSAGE_TYPE_RECV_TEXT;
     * 如果PrivateChatMessage的type为MessageType.IMAGE，且isSelfSend为true,则itemType是MESSAGE_TYPE_SENT_IMAGE,isSelfSend为false,则itemType是MESSAGE_TYPE_RECV_IMAGE;
     */

    init {
        // 注册消息类型与布局
        addItemType(MESSAGE_TYPE_SENT_TEXT, R.layout.item_sent_text)
        addItemType(MESSAGE_TYPE_RECV_TEXT, R.layout.item_received_text)
        addItemType(MESSAGE_TYPE_SENT_IMAGE, R.layout.item_sent_image)
        addItemType(MESSAGE_TYPE_RECV_IMAGE, R.layout.item_received_image)
        addItemType(MESSAGE_TYPE_SENT_LOCATION, R.layout.item_sent_location)
        addItemType(MESSAGE_TYPE_RECV_LOCATION, R.layout.item_received_location)
        addItemType(MESSAGE_TYPE_SYSTEM, R.layout.item_system_message)
    }

    override fun convert(holder: BaseViewHolder, item: PrivateChatMessage) {
        val isSender = item.isSelfSend

        when (item.itemType) {
            MESSAGE_TYPE_SENT_TEXT, MESSAGE_TYPE_RECV_TEXT -> handleTextMessage(holder, item, isSender)
         //   MESSAGE_TYPE_SENT_IMAGE, MESSAGE_TYPE_RECV_IMAGE -> handleImageMessage(holder, item, isSender)
          //  MESSAGE_TYPE_SENT_LOCATION, MESSAGE_TYPE_RECV_LOCATION -> handleLocationMessage(holder, item, isSender)
            MESSAGE_TYPE_SYSTEM -> handleSystemMessage(holder, item)
        }

    }

    private fun handleTextMessage(holder: BaseViewHolder, message: PrivateChatMessage, isSender: Boolean) {

        // 时间戳
        holder.setText(R.id.tv_timestamp, TimestampUtil.getTimePoint(message.sendTime))

        // 消息内容
        holder.setText(R.id.tv_chat_content, message.content)

        // 头像
        val ivTou = holder.getView<HeadImageView>(R.id.sdv_avatar)
        if (isSender) {
            ivTou.setName(MMKV.defaultMMKV().decodeString("userName"))
            ivTou.setUrl("")
        } else {
            ivTou.setName(mFriend.friendNickName)
            ivTou.setUrl("")
        }

        //TODO 消息状态 待定
//        if(isSender){//只有发送者，还有这个布局
//            // 消息状态
//            val sendingPb = holder.getView<View>(R.id.pb_sending)
//            val statusIv = holder.getView<View>(R.id.iv_msg_status)
//            when (message.status) {
//                MessageStatus.SENDED.code -> {
//                    sendingPb.visibility = View.GONE
//                    statusIv.visibility = View.GONE
//                }
//                MessageStatus.UNSEND.code -> {
//                    sendingPb.visibility = View.GONE
//                    statusIv.visibility = View.VISIBLE
//                }
//                else -> {}
//            }
//        }


        // 时间戳隐藏逻辑
        val position = data.indexOf(message)
        if (position > 0) {
            val lastMessage = data[position - 1]
            if (message.sendTime - lastMessage.sendTime < 10 * 60 * 1000) {
                holder.setGone(R.id.tv_timestamp, false)
            }
        }


    }

    //TODO 图片待处理
//    private fun handleImageMessage(holder: BaseViewHolder, message: PrivateChatMessage, isSender: Boolean) {
//        val imageMap = JSON.parseObject(message.messageBody, Map::class.java)
//        val imgUrl = imageMap["imgUrl"]?.toString() ?: ""
//        val localPath = imageMap["localPath"]?.toString() ?: ""
//
//        // 时间戳和头像
//        holder.setText(R.id.tv_timestamp, TimestampUtil.getTimePoint(message.sendTime))
//
//        // 头像
//        val ivTou = holder.getView<CircleHead>(R.id.sdv_avatar)
//        if (isSender) {
//            ivTou.setmText(MMKV.defaultMMKV().decodeString("userName"))
//        } else {
//            ivTou.setmText(mFriend.friendNickName)
//        }
//
//        // 图片加载
//        val imageContentSdv = holder.getView<SimpleDraweeView>(R.id.sdv_image_content)
//        val uri = if (localPath.isNotEmpty()) {
//            Uri.fromFile(File(localPath))
//        } else {
//            Uri.parse(OssUtil.resize(imgUrl))
//        }
//
//        val controller = Fresco.newDraweeControllerBuilder()
//            .setUri(uri)
//            .setOldController(imageContentSdv.controller)
//            .setControllerListener(object : ControllerListener<ImageInfo> {
//                override fun onSubmit(id: String?, callerContext: Any?) {}
//
//                override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
//                    imageInfo?.let {
//                        val originWidth = it.width
//                        val originHeight = it.height
//                        val adjustWidth = when {
//                            originWidth < originHeight -> DEFAULT_WIDTH_1
//                            originWidth > originHeight -> DEFAULT_WIDTH_3
//                            else -> DEFAULT_WIDTH_2
//                        }
//
//                        val params = imageContentSdv.layoutParams
//                        params.width = adjustWidth
//                        val resetHeight = CalculateUtil.mul(CalculateUtil.div(originHeight.toDouble(), originWidth.toDouble(), 5), adjustWidth.toDouble())
//                        params.height = resetHeight.toInt()
//                        imageContentSdv.layoutParams = params
//                    }
//                }
//
//                override fun onIntermediateImageSet(id: String?, imageInfo: ImageInfo?) {}
//                override fun onIntermediateImageFailed(id: String?, throwable: Throwable?) {}
//                override fun onFailure(id: String?, throwable: Throwable?) {}
//                override fun onRelease(id: String?) {}
//            })
//            .build()
//
//        imageContentSdv.controller = controller
//
//        // 点击查看大图
//        imageContentSdv.setOnClickListener {
//            val intent = Intent(mContext, MessageBigImageActivity::class.java)
//            intent.putExtra("imgUrl", imgUrl)
//            intent.putExtra("localPath", localPath)
//            mContext.startActivity(intent)
//        }
//    }

    //TODO 位置暂不支持
//    private fun handleLocationMessage(holder: BaseViewHolder, message: PrivateChatMessage, isSender: Boolean) {
//        val locationMap = JSON.parseObject(message.messageBody, Map::class.java)
//        val address = locationMap["address"]?.toString() ?: ""
//        val latitude = locationMap["latitude"]?.toString()?.toDoubleOrNull() ?: 0.0
//        val longitude = locationMap["longitude"]?.toString()?.toDoubleOrNull() ?: 0.0
//        val addressDetail = locationMap["addressDetail"]?.toString() ?: ""
//        val path = locationMap["path"]?.toString() ?: ""
//
//        holder.setText(R.id.tv_address, if (address.length > 15) "${address.substring(0, 11)}..." else address)
//        holder.setText(R.id.tv_address_detail, addressDetail)
//
//        if (path.isNotEmpty()) {
//            holder.getView<SimpleDraweeView>(R.id.sdv_location_img)?.setImageURI(Uri.parse(path))
//        }
//
//        holder.getView<View>(R.id.cv_chat_content)?.setOnClickListener {
//            val intent = Intent(mContext, MapPickerActivity::class.java)
//            intent.putExtra("latitude", latitude)
//            intent.putExtra("longitude", longitude)
//            intent.putExtra("sendLocation", false)
//            mContext.startActivity(intent)
//        }
//    }

    /**
     * 提示消息
     */
    private fun handleSystemMessage(holder: BaseViewHolder, message: PrivateChatMessage) {
        if(message.type == MessageType.TIP_TEXT.code){//文字提示
            holder.setText(R.id.tv_tip, message.content)
        }else if(message.type == MessageType.TIP_TIME.code){//时间提示
            holder.setText(R.id.tv_tip, TimestampUtil.getTimePoint(message.sendTime))

            if (data.indexOf(message) > 0) {
                val lastMessage = data[data.indexOf(message) - 1]
                if (message.sendTime - lastMessage.sendTime < 10 * 60 * 1000) {
                    holder.setGone(R.id.tv_tip, false)
                }
            }
        }
    }


    companion object {
        const val MESSAGE_TYPE_SENT_TEXT = 0
        const val MESSAGE_TYPE_RECV_TEXT = 1
        const val MESSAGE_TYPE_SENT_IMAGE = 2
        const val MESSAGE_TYPE_RECV_IMAGE = 3
        const val MESSAGE_TYPE_SENT_LOCATION = 4
        const val MESSAGE_TYPE_RECV_LOCATION = 5
        const val MESSAGE_TYPE_SYSTEM = 6

        private const val DEFAULT_WIDTH_1 = 300
        private const val DEFAULT_WIDTH_2 = 400
        private const val DEFAULT_WIDTH_3 = 500
    }
}
