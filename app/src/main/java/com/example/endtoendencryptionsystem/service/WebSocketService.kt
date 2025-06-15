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
import androidx.core.app.NotificationCompat
//import com.example.endtoendencryptionsystem.MainActivity
import com.example.endtoendencryptionsystem.R
import com.example.endtoendencryptionsystem.http.Config
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import java.net.URI
import java.util.Date
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
    private var heartBeatTask: ScheduledFuture<*>? = null
    
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

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WebSocket服务已创建")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
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
                    
                    // 启动心跳检测
                    startHeartbeat()
                    
                    // 通知UI
                    handler.post {
                        connectCallback?.invoke()
                    }
                }
                
                override fun onMessage(message: String?) {
                    Log.d(TAG, "收到消息: $message")
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
        sendMessage(objectMapper.writeValueAsString(loginNode))
    }
    
    /**
     * 启动心跳检测
     */
    private fun startHeartbeat() {
        stopHeartbeat()
        
        heartBeatTask = scheduler.scheduleAtFixedRate({
            if (isConnected && webSocketClient != null && webSocketClient!!.isOpen) {
                val heartbeatNode = objectMapper.createObjectNode().apply {
                    put("cmd", 1)
                    set<ObjectNode>("data", objectMapper.createObjectNode())
                }
                Log.d(TAG, "发送心跳")
                sendMessage(objectMapper.writeValueAsString(heartbeatNode))
            }
        }, 0, HEART_BEAT_INTERVAL, TimeUnit.MILLISECONDS)
    }
    
    /**
     * 停止心跳检测
     */
    private fun stopHeartbeat() {
        heartBeatTask?.let {
            if (!it.isCancelled) {
                it.cancel(true)
            }
        }
        heartBeatTask = null
    }

    /**
     * 解析消息
     */
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

}
