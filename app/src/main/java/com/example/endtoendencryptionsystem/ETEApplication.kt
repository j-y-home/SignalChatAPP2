package com.example.endtoendencryptionsystem

//import com.therouter.TheRouter
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.lnsoft.conslutationsystem.core.AppActivityManager
import com.tencent.mmkv.MMKV
import io.dcloud.feature.sdk.DCSDKInitConfig
import io.dcloud.feature.sdk.DCUniMPSDK
import io.dcloud.feature.sdk.Interface.IDCUniMPPreInitCallback
import io.dcloud.feature.sdk.MenuActionSheetItem


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
}