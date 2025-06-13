//package com.example.endtoendencryptionsystem.service
//
//import android.app.NotificationManager
//import android.app.Service
//import android.content.Intent
//import android.os.Handler
//import android.os.IBinder
//import android.util.Log
//import com.example.endtoendencryptionsystem.http.Config
//import com.tencent.mmkv.MMKV
//import org.java_websocket.client.WebSocketClient
//import org.java_websocket.handshake.ServerHandshake
//import java.net.URI
//import java.util.concurrent.TimeUnit
//
//
//class WebSocketService : Service() {
//    private var webSocket: MsgWebSocketClient? = null
//    private var mManager: NotificationManager? = null
//    override fun onCreate() {
//        super.onCreate()
//        Log.e("xxx","创建服务")
//        initSocketClient()
//        //开启心跳检测
//        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE)
//    }
//
//    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
//        Log.e("xxx","启动服务")
//        if (webSocket != null && webSocket!!.isOpen) {
//            //启动service时调用，可以检查websocket此时状态
//            Log.e("xxxx","websocket启动了")
//        }
//        return START_STICKY
//    }
//
//
//    override fun onBind(intent: Intent): IBinder? {
//        return null
//    }
//
//    override fun onDestroy() {
//        closeConnect()
//        Log.e("xxxx","service销毁。ws被销毁");
//        super.onDestroy()
//    }
//
//
//    private fun initSocketClient() {
//        // 初始化WebSocket连接
//        webSocket = object : MsgWebSocketClient(URI.create(Config.receiveMessageURL)) {
//            override fun onOpen(handshakedata: ServerHandshake) {
//                val token = MMKV.defaultMMKV().decodeString("token")
//                WebSocketClient.send("{\"token\":\"$token\"}")
//
//                isConnected = true;
//                sendLoginCommand();
//                startHeartbeat();
//                if (connectCallback != null) {
//                    connectCallback.onConnect();
//                }
//            }
//
//            override fun onMessage(message: String?) {
//                Log.e("xxx", "websocket收到消息:==>$message")
//                handleMessage(message)
//            }
//
//            override fun onClose(code: Int, reason: String?, remote: Boolean) {
//                Log.e("xxx", "onClose() 连接断开_reason：$reason");
//                mHandler.removeCallbacks(heartBeatRunnable);
//                //开启心跳检测
//                mHandler.postDelayed(heartBeatRunnable, CLOSE_RECON_TIME);
//            }
//
//            override fun onError(ex: java.lang.Exception?) {
//                Log.e("xxxx", "onError() 连接出错：" + ex!!.message)
//                mHandler.removeCallbacks(heartBeatRunnable);
//                //开启心跳检测
//                mHandler.postDelayed(heartBeatRunnable, CLOSE_RECON_TIME);
//
//            }
//
//        }
//        connect();
//    }
//
//    private fun connect() {
//        Thread(){
//           run {
//               try {
//                   webSocket!!.connectBlocking()
//               } catch (e:InterruptedException) {
//                   e.printStackTrace();
//               }
//           }
//        }.start()
//
//    }
//
//    fun sendMsg(msg: String?) {
//        Thread {
//            if (null != webSocket && webSocket!!.isOpen) {
//                webSocket!!.send(msg)
//            }
//        }.start()
//    }
//
//    private fun closeConnect() {
//        try {
//            if (null != webSocket) {
//                webSocket!!.close()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        } finally {
//            webSocket = null
//        }
//    }
//
//    //每隔10秒进行一次对长连接的心跳检测
//    private val HEART_BEAT_RATE: Long = (10 * 1000).toLong()
//    //错误立即重连
//    private val CLOSE_RECON_TIME: Long = (3 * 100).toLong()
//    private val mHandler: Handler = Handler()
//    private val heartBeatRunnable: Runnable = object : Runnable {
//        override fun run() {
//            if (webSocket != null) {
//                if (webSocket!!.isClosed) {
//                    reconnectWs()
//                }
//            } else {
//                //如果client已为空，重新初始化连接
//                initSocketClient()
//            }
//            //每隔一定的时间，对长连接进行一次心跳检测
//            mHandler.postDelayed(this, HEART_BEAT_RATE)
//        }
//    }
//
//    private fun reconnectWs() {
//        mHandler.removeCallbacks(heartBeatRunnable)
//        object : Thread() {
//            override fun run() {
//                try {
//                    sleep(1000)
//                    Log.e("Websocket", "开启重连")
//                    if(webSocket != null){
//                        webSocket!!.reconnectBlocking()
//                    }
//                } catch (e: InterruptedException) {
//                    e.printStackTrace()
//                }
//            }
//        }.start()
//    }
//
//
//    /**
//     *
//     */
//    private fun handleMessage(message: String) {
//        try {
//            val msgObj: JSONObject = JSONObject(message)
//            val cmd: Int = msgObj.getInt("cmd")
//            val msgInfo: JSONObject? = msgObj.optJSONObject("data")
//
//            let sendInfo = JSON.parse(res.data)
//            if (sendInfo.cmd == 0) {
//                heartCheck.start()
//                connectCallBack && connectCallBack();
//                console.log('WebSocket登录成功')
//            } else if (sendInfo.cmd == 1) {
//                // 重新开启心跳定时
//                heartCheck.reset();
//            }
//            when (cmd) {
//                0 -> {
//                    startHeartbeat()
//                    if (messageListener != null) {
//                        messageListener.onConnected()
//                    }
//                }
//
//                1 -> resetHeartbeat()
//                2 -> showForceLogoutDialog()
//                3 -> handlePrivateMessage(msgInfo)
//                4 -> handleGroupMessage(msgInfo)
//                5 -> handleSystemMessage(msgInfo)
//            }
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun sendLoginCommand() {
//        val loginJson = "{ \"cmd\": 0, \"data\": { \"accessToken\": \"" + accessToken + "\" }}"
//        webSocket!!.send(loginJson)
//    }
//    private fun startHeartbeat() {
//        stopHeartbeat()
//        heartBeatTask = scheduler.scheduleAtFixedRate({
//            if (isConnected && webSocket != null) {
//                val heartbeat = "{ \"cmd\": 1, \"data\": {} }"
//                webSocket!!.send(heartbeat)
//            }
//        }, 0, 20, TimeUnit.SECONDS)
//    }
//
//    private fun stopHeartbeat() {
//        if (heartBeatTask != null && !heartBeatTask.isCancelled()) {
//            heartBeatTask.cancel(true)
//        }
//    }
//}
