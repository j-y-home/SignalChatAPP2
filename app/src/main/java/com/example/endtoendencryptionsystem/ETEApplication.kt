package com.example.endtoendencryptionsystem

//import com.therouter.TheRouter
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.endtoendencryptionsystem.entiy.vo.LoginVO
import com.example.endtoendencryptionsystem.http.Config
import com.example.endtoendencryptionsystem.http.Interceptors
import com.example.endtoendencryptionsystem.service.MyWebSocketClient
import com.example.endtoendencryptionsystem.service.WebSocketManager
import com.example.endtoendencryptionsystem.utils.json
import com.example.endtoendencryptionsystem.utils.toJSONString
import com.lnsoft.conslutationsystem.core.AppActivityManager
import com.tencent.mmkv.MMKV
import decodeParcelableCompat
import io.dcloud.feature.sdk.DCSDKInitConfig
import io.dcloud.feature.sdk.DCUniMPSDK
import io.dcloud.feature.sdk.Interface.IDCUniMPPreInitCallback
import io.dcloud.feature.sdk.MenuActionSheetItem
import org.json.JSONObject
import java.net.URI


class ETEApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        baseApplication = this
        registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {
                AppActivityManager.setCurrentActivity(activity)
            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }

        })
        MMKV.initialize(this)
        if (BuildConfig.DEBUG) {
            Interceptors.openLog()
        }

        initWebsocetClient()
        //开启网络请求日志打印
     //   if (BuildConfig.DEBUG) Interceptors.openLog()
//        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
//            ClassicsHeader(context)
//        }
//        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
//            ClassicsFooter(context)
//        }
//        var filePath = getExternalFilesDir("")!!.absolutePath
//        val file = File(filePath)
//        if (!file.exists()) {
//            file.mkdirs()
//        }
//        val config: DCSDKInitConfig = Builder()
//            .setCapsule(true)
//            .setMenuDefFontSize("16px")
//            .setMenuDefFontColor("#ff00ff")
//            .setMenuDefFontWeight("normal")
//            .setMenuActionSheetItems(sheetItems)
//            .build()
        //初始化 uni小程序SDK ----start----------
        val item: MenuActionSheetItem = MenuActionSheetItem("关于", "gy")

        val item1: MenuActionSheetItem = MenuActionSheetItem("获取当前页面url", "hqdqym")
        val item2: MenuActionSheetItem =
            MenuActionSheetItem("跳转到宿主原生测试页面", "gotoTestPage")
        val sheetItems: MutableList<MenuActionSheetItem> = ArrayList<MenuActionSheetItem>()
        sheetItems.add(item)
        sheetItems.add(item1)
        sheetItems.add(item2)
        Log.i("unimp", "onCreate----")
        val config: DCSDKInitConfig = DCSDKInitConfig.Builder()
            .setCapsule(false)
            .setMenuDefFontSize("16px")
            .setMenuDefFontColor("#ff00ff")
            .setMenuDefFontWeight("normal")
            .setMenuActionSheetItems(sheetItems)
            .setEnableBackground(true) //开启后台运行
            .setUniMPFromRecents(true)
            .build()
        DCUniMPSDK.getInstance().initialize(this, config, object : IDCUniMPPreInitCallback {
            override fun onInitFinished(b: Boolean) {
                Log.d("unimpaa", "onInitFinished----$b")
            }
        })
        //初始化 uni小程序SDK ----end----------

}

    override fun attachBaseContext(base: Context?) {
        if (BuildConfig.DEBUG){
        //    TheRouter.isDebug = true
        }
        super.attachBaseContext(base)
    }

    companion object {
        var baseApplication: ETEApplication? = null
        @JvmStatic
        fun getInstance(): ETEApplication? {
            return baseApplication
        }
    }

    private fun initWebsocetClient(){
        val loginInfo: LoginVO? = MMKV.defaultMMKV().decodeParcelableCompat<LoginVO>("loginInfo")
        var accessToken:String? = ""
        if(loginInfo!=null){
            accessToken = loginInfo.accessToken?:""
        }
        val client = MyWebSocketClient(URI.create(Config.receiveMessageURL), accessToken.toString()).apply {
            setConnectCallback {
                Log.d("WebSocket", "Connected")
            }
            setMessageCallback { cmd, data ->
                Log.d("WebSocket", "Received CMD: $cmd, Data: $data")
            }

            setCloseCallback { code ->
                Log.d("WebSocket", "Connection closed with code $code")
            }
        }
        client.connect()
    }



    private fun initWebSocket() {
        val wsUrl = Config.receiveMessageURL
        val loginInfo: LoginVO? = MMKV.defaultMMKV().decodeParcelableCompat<LoginVO>("loginInfo")
        var accessToken:String? = ""
        if(loginInfo!=null){
            accessToken = loginInfo.accessToken?:""
        }
        val manager: WebSocketManager = WebSocketManager.getInstance()

        manager.connect(wsUrl, accessToken)

        manager.setOnMessageCallback({ message ->
            Log.d("WebSocket", "setOnMessageCallback: ${json.toJSONString(message)}")
            try {
                val json: JSONObject = JSONObject(message)
                val cmd: Int = json.getInt("cmd")
                val data: Any? = json.get("data")
                when (cmd) {
                    2 -> handleForceLogout()
                    3 -> handlePrivateMessage(data)
                    4 -> handleGroupMessage(data)
                    5 -> handleSystemMessage(data)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        manager.setOnConnectCallback {
            Log.d("WebSocket", "连接成功")
        }

        manager.setOnCloseCallback { code, reason ->
            Log.d("WebSocket", "连接关闭: $reason"+"  开启重连")
            // 触发重连机制
            reconnectWebSocket()
        }
    }


    private fun reconnectWebSocket() {
        Handler(Looper.getMainLooper()).postDelayed({
            val wsUrl = Config.receiveMessageURL
            val loginInfo: LoginVO? = MMKV.defaultMMKV().decodeParcelableCompat<LoginVO>("loginInfo")
            var accessToken:String? = ""
            if(loginInfo!=null){
                accessToken = loginInfo.accessToken?:""
            }
            WebSocketManager.getInstance().connect(wsUrl, accessToken)
        }, 10000) // 10秒后重连
    }



    private fun handleForceLogout() {
        // 发送广播通知所有页面退出到登录页
        val intent: Intent = Intent("ACTION_FORCE_LOGOUT")
        sendBroadcast(intent)
    }

    private fun handlePrivateMessage(data: Any?) {
        // 处理私聊消息，插入数据库、更新UI等
        Log.e("xxx","接收到私聊消息："+ json.toJSONString(data!!))
    }

    private fun handleGroupMessage(data: Any?) {
        // 处理群聊消息
        Log.e("xxx","接收到群聊消息："+ json.toJSONString(data!!))
    }

    private fun handleSystemMessage(data: Any?) {
        Log.e("xxx","接收到系统消息："+ json.toJSONString(data!!))
        // 处理系统消息
    }
}