package com.example.endtoendencryptionsystem.service

import android.content.Intent
import android.util.Log
import com.example.endtoendencryptionsystem.ETEApplication
import com.example.endtoendencryptionsystem.entiy.database.PrivateChatMessage
import com.example.endtoendencryptionsystem.enums.MessageType
import com.tencent.mmkv.MMKV
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONException
import org.json.JSONObject
import java.net.URI
import java.util.concurrent.TimeUnit

/**
 * TODO bug :每隔20秒，会连续两次收到命令1
 */
class MyWebSocketClient(
    serverUri: URI,
    private var accessToken: String
) : WebSocketClient(serverUri) {

    private var messageCallback: ((cmd: Int, data: JSONObject) -> Unit)? = null
    private var connectCallback: (() -> Unit)? = null
    private var closeCallback: ((code: Int) -> Unit)? = null

    private val heartCheck = object {
        var timeout = 20000L
        var timeoutObj: Disposable? = null

        fun start() {
            if (this@MyWebSocketClient.isClosed.not()) {
                val heartbeat = "{ \"cmd\": 1, \"data\": {} }"
                Log.e("WebSocket","发送命令：1")
                send(heartbeat)
            }
        }



        fun reset() {
            Log.d("WebSocket", "收到命令：重置方法")
            timeoutObj?.dispose()
            timeoutObj = null
            timeoutObj = Observable.timer(timeout, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { start() }
        }

    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        // 发送登录命令
        val loginJson = "{ \"cmd\": 0, \"data\": { \"accessToken\": \"" + accessToken + "\" }}"
        send(loginJson)
        connectCallback?.invoke()
    }

    override fun onMessage(message: String?) {
        message?.let {
            try {
                val jsonObject = JSONObject(it)
                val cmd = jsonObject.getInt("cmd")
                when (cmd) {
                    0 -> {
                        Log.d("WebSocket", "收到命令：0")
                        heartCheck.start()
                        connectCallback?.invoke()
                    }
                    1 -> {
                        Log.d("WebSocket", "收到命令：1")
                        heartCheck.reset()
                    }
                    2 -> {//异地登录，强制退出
                        // 发送广播通知所有页面退出到登录页
                        val intent: Intent = Intent("ACTION_FORCE_LOGOUT")
                        ETEApplication.baseApplication!!.sendBroadcast(intent)
                    }
                    3 -> {//收到私聊消息
                        val data = jsonObject.getJSONObject("data")
                        handlePrivateMessage(data)
                    }
                    else -> {
                        Log.d("WebSocket", "收到命令："+cmd)
                        val data = jsonObject.getJSONObject("data")
                        messageCallback?.invoke(cmd, data)
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        closeCallback?.invoke(code)
    }

    override fun onError(ex: Exception?) {
        ex?.printStackTrace()
    }

    fun setMessageCallback(callback: (cmd: Int, data: JSONObject) -> Unit) {
        this.messageCallback = callback
    }

    fun setConnectCallback(callback: () -> Unit) {
        this.connectCallback = callback
    }

    fun setCloseCallback(callback: (code: Int) -> Unit) {
        this.closeCallback = callback
    }

    fun reconnect(wsUrl: String, token: String) {
        accessToken = token
        this.uri = URI.create(wsUrl)
        this.reconnect()
    }

    fun sendMessage(message: String) {
        if (!this.isClosed) {
            send(message)
        }
    }


    /**
     * 收到私聊消息
     */
    fun handlePrivateMessage(dataJson: JSONObject) {
        try {
            val msg = PrivateChatMessage()
            msg.messageId = dataJson.getString("id")
            msg.sendId = dataJson.getLong("sendId")
            msg.recvId = dataJson.getLong("recvId")
            msg.type = dataJson.getInt("type")
            msg.content = dataJson.getString("content")
            msg.sendTime = dataJson.getLong("sendTime")
            msg.status = dataJson.getInt("status")
            msg.isSelfSend = msg.sendId == MMKV.defaultMMKV().decodeInt("userId").toLong()


            // 好友ID：根据发送/接收方确定
            val friendId = if (msg.isSelfSend) msg.recvId else msg.sendId

            // 构造会话信息
            val chatInfo = ChatInfo(
                type = ChatType.PRIVATE,
                targetId = friendId,
                showName = getFriendNickName(friendId),
                headImage = getFriendAvatar(friendId)
            )

            // 加载标志
            if (msg.type == MessageType.LOADING.code) {
                setLoadingPrivateMsg(JSONObject(msg.content).getBoolean("isLoading"))
                return
            }

            // 已读处理
            if (msg.type == MessageType.READED.code) {
                resetUnreadCount(chatInfo)
                return
            }

            // 回执处理
            if (msg.type == MessageType.RECEIPT.code) {
                updateMessageReadStatus(msg.id, chatInfo)
                return
            }

            // 撤回处理
            if (msg.type == MessageType.RECALL.code) {
                recallMessage(msg, chatInfo)
                return
            }

            // 新增好友
            if (msg.type == MessageType.FRIEND_NEW.code) {
                val friend = parseFriendFromContent(msg.content)
                addFriendToDb(friend)
                return
            }

            // 添加好友提示（特殊文本）
            if (msg.type == MessageType.TIP_TEXT.code && msg.content == "你们已成为好友，现在可以开始聊天了") {
                addFriendToAndroidDb(friendId)
                return
            }

            // 删除好友
            if (msg.type == MessageType.FRIEND_DEL.code) {
                removeFriend(friendId)
                return
            }

            // Android Hook 处理
            usePrivateMessageAndroidHook(msg, friendId)

            // 插入消息
            insertPrivateMessage(chatInfo, msg)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    /**
     * 插入私聊消息
     */
    fun insertPrivateMessage(chatInfo: ChatInfo, msg: PrivateChatMessage) {
        //TODO 打开会话？

        // 普通消息、提示消息、动作消息
        if (isNormalMessageType(msg.type) || isTipMessageType(msg.type) || isActionMessageType(msg.type)) {
            // 解密消息（如果是文本消息）
            if (msg.type == MessageType.TEXT.code) {
                decryptPrivateMessage(msg.content, chatInfo.targetId).observeForever { decryptContent ->
                    msg.content = decryptContent
                    chatStore.insertMessage(msg, chatInfo)
                }
            } else {
                chatStore.insertMessage(msg, chatInfo)
            }
        }
    }

    fun isNormalMessageType(type: Int): Boolean {
        return type >= 0 && type < 10
    }

    fun isTipMessageType(type: Int): Boolean {
        return type >= 10 && type < 20
    }

    fun isActionMessageType(type: Int): Boolean {
        return type >= 20 && type < 30
    }


}


