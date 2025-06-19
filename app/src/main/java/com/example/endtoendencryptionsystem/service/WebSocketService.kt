package com.example.endtoendencryptionsystem.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.alibaba.fastjson.JSONObject
import com.example.endtoendencryptionsystem.ETEApplication
//import com.example.endtoendencryptionsystem.MainActivity
import com.example.endtoendencryptionsystem.R
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.vo.FriendVO
import com.example.endtoendencryptionsystem.entiy.vo.PrivateMessageVO
import com.example.endtoendencryptionsystem.entiy.vo.WebsocketMsgVO
import com.example.endtoendencryptionsystem.enums.ConversationType
import com.example.endtoendencryptionsystem.enums.MessageType
import com.example.endtoendencryptionsystem.http.Config
import com.example.endtoendencryptionsystem.repository.AppDatabase
import com.example.endtoendencryptionsystem.repository.ChatMsgRepository
import com.example.endtoendencryptionsystem.repository.FriendRepository
import com.example.endtoendencryptionsystem.utils.EncryptionUtil
import com.example.endtoendencryptionsystem.utils.json
import com.example.endtoendencryptionsystem.utils.toFriend
import com.example.endtoendencryptionsystem.utils.toJSONString
import com.example.endtoendencryptionsystem.utils.toObject
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.tencent.mmkv.MMKV
import java.net.URI
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


class WebSocketService : Service() {
    companion object {
        private const val TAG = "WebSocketService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "websocket_service_channel"
        private const val HEART_BEAT_INTERVAL = 20000L // 心跳间隔，单位毫秒
        private const val RECONNECT_INTERVAL = 10000L // 重连间隔，单位毫秒
    }
    
    // WebSocket客户端
    private var webSocketClient: WebSocketClient? = null
    private val handler = Handler(Looper.getMainLooper())
    private val scheduler: ScheduledExecutorService = ScheduledThreadPoolExecutor(1)
  //  private var heartBeatTask: ScheduledFuture<*>? = null
    private var heartbeatRunnable: Runnable? = null


    private var heartbeatTimer: Timer? = null
    
    // 连接状态
    private var isConnected = false
    private var lastConnectTime = Date() // 最后一次连接时间
    private var serverUri: URI? = null
    private var accessToken: String = ""
    
    // 回调接口
    private var messageCallback: ((cmd: Int, data: JsonNode) -> Unit)? = null
    private val objectMapper = ObjectMapper()
    private var connectCallback: (() -> Unit)? = null
    private var closeCallback: ((code: Int) -> Unit)? = null
    
    // 通知管理器
    private var notificationManager: NotificationManager? = null
    
    // Binder提供给Activity绑定
    inner class WebSocketBinder : Binder() {
        fun getService(): WebSocketService = this@WebSocketService
    }
    
    private val binder = WebSocketBinder()

    private val friendRepository = FriendRepository(ETEApplication.getInstance()!!)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WebSocket服务已创建")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "WebSocket服务已启动")
        intent?.let {
            if (it.hasExtra("wsUrl") && it.hasExtra("token")) {
                val wsUrl = it.getStringExtra("wsUrl")
                val token = it.getStringExtra("token") ?: ""
                
                // 连接WebSocket
                connect(wsUrl ?: Config.receiveMessageURL, token)
            }
        }
        
        return START_STICKY // 服务被杀死后会尝试重启
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        Log.d(TAG, "WebSocket服务被销毁")
        closeWebSocket()
        super.onDestroy()
    }


    /**
     * 连接WebSocket
     * @param wsUrl WebSocket URL
     * @param token 访问令牌
     */
    fun connect(wsUrl: String, token: String) {
        if (isConnected) {
            Log.d(TAG, "WebSocket已连接，无需重复连接")
            return
        }
        
        accessToken = token
        serverUri = URI.create(wsUrl)
        lastConnectTime = Date()
        
        initWebSocketClient()
    }
    
    /**
     * 初始化WebSocket客户端
     */
    private fun initWebSocketClient() {
        // 确保先关闭旧连接
        closeWebSocket()
        
        serverUri?.let { uri ->
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    Log.d(TAG, "WebSocket已连接")
                    isConnected = true
                    
                    // 发送登录命令
                    sendLoginCommand()
                    
//                    // 启动心跳检测 TODO 先注释掉，js没在这儿启动
//                    startHeartbeat()
                    
                    // 通知UI
                    handler.post {
                        connectCallback?.invoke()
                    }
                }
                
                override fun onMessage(message: String?) {
                    Log.d(TAG, "onMessage: $message")
                    message?.let {
                        parseMessage(it)
                    }
                }
                
                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d(TAG, "WebSocket已关闭: $code, $reason")
                    isConnected = false
                    stopHeartbeat()
                    
                    // 通知UI
                    handler.post {
                        closeCallback?.invoke(code)
                    }
                    
                    // 断线重连
                    scheduleReconnect()
                }
                
                override fun onError(ex: Exception?) {
                    Log.e(TAG, "WebSocket错误: ${ex?.message}")
                    ex?.printStackTrace()
                }
            }
            
            // 在后台线程中连接WebSocket
            Thread {
                try {
                    webSocketClient?.connectBlocking()
                } catch (e: Exception) {
                    Log.e(TAG, "WebSocket连接失败", e)
                    scheduleReconnect()
                }
            }.start()
        }
    }
    
    /**
     * 关闭WebSocket连接
     */
    private fun closeWebSocket() {
        try {
            stopHeartbeat()
            webSocketClient?.let {
                if (it.isOpen) {
                    it.close()
                }
            }
            webSocketClient = null
            isConnected = false
        } catch (e: Exception) {
            Log.e(TAG, "关闭WebSocket失败", e)
        }
    }
    
    /**
     * 重连WebSocket
     */
    fun reconnect() {
        if (serverUri != null && accessToken.isNotEmpty()) {
            connect(serverUri.toString(), accessToken)
        } else {
            Log.e(TAG, "无法重连WebSocket：缺少必要参数")
        }
    }
    
    /**
     * 安排重连
     */
    private fun scheduleReconnect() {
        // 避免频繁重连
        val timeDiff = Date().time - lastConnectTime.time
        val delay = if (timeDiff < RECONNECT_INTERVAL) RECONNECT_INTERVAL - timeDiff else 0
        
        handler.postDelayed({
            Log.d(TAG, "尝试重新连接WebSocket...")
            reconnect()
        }, delay)
    }

    /**
     * 发送消息
     * @param message 消息内容
     */
    fun sendMessage(message: String) {
        Thread {
            if (isConnected && webSocketClient != null && webSocketClient!!.isOpen) {
                webSocketClient!!.send(message)
            } else {
                Log.e(TAG, "WebSocket未连接，无法发送消息")
                reconnect()
            }
        }.start()
    }
    
    /**
     * 发送登录命令
     */
    private fun sendLoginCommand() {
        val loginNode = objectMapper.createObjectNode().apply {
            put("cmd", 0)
            set<ObjectNode>("data", objectMapper.createObjectNode().apply {
                put("accessToken", accessToken)
            })
        }
        Log.d(TAG,"发送的登录命令：${objectMapper.writeValueAsString(loginNode)}")
        sendMessage(objectMapper.writeValueAsString(loginNode))
    }
    
    /**
     * 启动心跳检测
     */
    private fun startHeartbeat() {
        stopHeartbeat()

        heartbeatRunnable = object : Runnable {
            override fun run() {
                if (isConnected && webSocketClient != null && webSocketClient!!.isOpen) {
                    sendHeartbeat()
                }
                handler.postDelayed(this, HEART_BEAT_INTERVAL)
            }
        }

        handler.postDelayed(heartbeatRunnable!!, HEART_BEAT_INTERVAL)
    }
    private fun sendHeartbeat() {
        val heartbeatNode = objectMapper.createObjectNode().apply {
            put("cmd", 1)
            set<ObjectNode>("data", objectMapper.createObjectNode())
        }
        try {
            val message = objectMapper.writeValueAsString(heartbeatNode)
            Log.d(TAG, "发送心跳包")
            sendMessage(message)
        } catch (e: Exception) {
            Log.e(TAG, "构建心跳包失败", e)
        }
    }


//    private fun startHeartbeat() {
//        stopHeartbeat()
//
//        heartBeatTask = scheduler.scheduleAtFixedRate({
//            if (isConnected && webSocketClient != null && webSocketClient!!.isOpen) {
//                val heartbeatNode = objectMapper.createObjectNode().apply {
//                    put("cmd", 1)
//                    set<ObjectNode>("data", objectMapper.createObjectNode())
//                }
//                Log.d(TAG, "发送心跳")
//                sendMessage(objectMapper.writeValueAsString(heartbeatNode))
//            }
//        }, 0, HEART_BEAT_INTERVAL, TimeUnit.MILLISECONDS)
//    }
    
    /**
     * 停止心跳检测
     */
    private fun stopHeartbeat() {
        heartbeatRunnable?.let { handler.removeCallbacks(it) }
        heartbeatRunnable = null
    }

//    private fun stopHeartbeat() {
//        heartBeatTask?.let {
//            if (!it.isCancelled) {
//                it.cancel(true)
//            }
//        }
//        heartBeatTask = null
//    }

//    fun resetHeartbeat() {
//        stopHeartbeat()
//        heartbeatTimer = Timer()
//        heartbeatTimer?.schedule(object : TimerTask() {
//            override fun run() {
//                sendHeartbeat()
//            }
//        }, 20000)
//    }
//
//    fun stopHeartbeat() {
//        heartbeatTimer?.cancel()
//        heartbeatTimer?.purge()
//        heartbeatTimer = null
//    }

    /**
     * 解析消息
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseMessage(message: String) {
        try {
            val rootNode = objectMapper.readTree(message)
            val cmd = rootNode.get("cmd").asInt()
            val data = rootNode.get("data")
            
            when (cmd) {
                0 -> { // 登录响应
                    Log.d(TAG, "收到登录响应")
                    startHeartbeat()
                    handler.post {
                        connectCallback?.invoke()
                    }
                }
                1 -> { // 心跳响应
                    Log.d(TAG, "收到心跳响应")
                    resetHeartbeat()
                }
                2 -> { // 异地登录，强制退出
                    Log.d(TAG, "收到异地登录通知")
                    // 发送广播通知所有页面退出到登录页
                    val intent = Intent("ACTION_FORCE_LOGOUT")
                    sendBroadcast(intent)
                }
                3 -> {//收到私聊消息
                    var msg = json.toObject<PrivateMessageVO>(data.toString())
                    handlePrivateMessage(msg)

                }
                else -> { // 其他消息
                    if (data != null && !data.isNull) {
                        handler.post {
                            messageCallback?.invoke(cmd, data)
                        }
                    }
                }
            }
        } catch (e: JsonProcessingException) {
            Log.e(TAG, "解析消息失败", e)
        }
    }
    
    /**
     * 重置心跳
     */
    private fun resetHeartbeat() {
        stopHeartbeat()
        startHeartbeat()
    }

    /**
     * 创建通知通道
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WebSocket服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持WebSocket连接的服务通道"
                setShowBadge(false)
            }
            
            notificationManager?.createNotificationChannel(channel)
        }
    }
    
    /**
     * 创建前台服务通知
     */
    private fun createNotification(): android.app.Notification {
//        val intent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0, intent,
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
//        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WebSocket服务")
            .setContentText("保持连接中...")
//            .setSmallIcon(R.drawable.notification) // 确保有此图标
//            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    /**
     * 设置消息回调
     */
    fun setMessageCallback(callback: (cmd: Int, data: JsonNode) -> Unit) {
        this.messageCallback = callback
    }
    
    /**
     * 设置连接回调
     */
    fun setConnectCallback(callback: () -> Unit) {
        this.connectCallback = callback
    }
    
    /**
     * 设置关闭回调
     */
    fun setCloseCallback(callback: (code: Int) -> Unit) {
        this.closeCallback = callback
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun handlePrivateMessage(msg: PrivateMessageVO) {
        // 好友ID：根据发送/接收方确定
        var isSelfSend = msg.sendId == MMKV.defaultMMKV().decodeInt("userId").toLong()
        val friendId = if (isSelfSend) msg.recvId else msg.sendId
        when (msg.type) {
            MessageType.FRIEND_NEW.code -> {
                Log.d(TAG,"收到的好友添加信息："+msg.content)
                val friendVO = json.toObject<FriendVO>(msg.content.toString())
                //保存好友信息并重置session
                friendRepository.saveAndUpdateSession(friendVO.toFriend())
            }
            MessageType.FRIEND_DEL.code -> {
                /**
                 * A删除B，在确定删除时调用接口同时删除数据库中的好友信息。服务器端推送消息
                 * A会收到：81的消息。
                 * B会收到：81和21的消息，在B的私聊表中插入一条21的“你们好友关系已解除”的提示消息
                 * TODO B端收到消息后做什么操作？在他数据库中删除A吗？待定
                 */
                Log.d(TAG,"收到好友解除关系的消息")
            }
            // 处理其他类型的消息...
            else -> {
                insertPrivateMessage(msg)
            }
        }



    }

    /**
     * 插入私聊消息
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun insertPrivateMessage(msg: PrivateMessageVO) {
        // 普通消息、提示消息、动作消息
        if (isNormalMessageType(msg.type) || isTipMessageType(msg.type) || isActionMessageType(msg.type)) {
            // 解密消息（如果是文本消息）
            if (msg.type == MessageType.TEXT.code) {
                Log.d(TAG,"收到好友发送的私聊消息："+json.toJSONString(msg))
                //先解密 再保存消息
                var deMsg = EncryptionUtil.decryptPrivateMessage(msg.sendId.toString(), msg.content.toString())
                msg.content = deMsg
                val chatStore = ChatMsgRepository(application)
                chatStore.saveChatConversation(msg, ConversationType.PRIVATE.type)
            } else {//直接保存
                Log.d(TAG,"收到其他类型的私聊消息："+msg.type+"||"+json.toJSONString(msg))
                val chatStore = ChatMsgRepository(application)
                chatStore.saveChatConversation(msg, ConversationType.PRIVATE.type)
            }
        }
    }

    fun isNormalMessageType(type: Int): Boolean {
        return type >= 0 && type < 10
    }

    fun isTipMessageType(type: Int): Boolean {
        return type >= 20 && type < 30
    }

    fun isActionMessageType(type: Int): Boolean {
        return type >= 40 && type < 50
    }

}
