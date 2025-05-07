package com.example.endtoendencryptionsystem.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.endtoendencryptionsystem.ETEApplication.Companion.getInstance
import com.example.endtoendencryptionsystem.databinding.ActivityUniappMainBinding
import com.example.endtoendencryptionsystem.repository.ChatRepository
import io.dcloud.feature.sdk.DCUniMPSDK
import io.dcloud.feature.sdk.Interface.IOnUniMPEventCallBack
import io.dcloud.feature.sdk.Interface.IUniMP
import io.dcloud.feature.unimp.DCUniMPJSCallback
import io.dcloud.feature.unimp.config.UniMPOpenConfiguration
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.util.KeyHelper


/**
 * 登录
 *
 * @author zhou
 */
class UniAPPMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUniappMainBinding
    var mContext: Context? = null
    var mHandler: Handler? = null
    private val chatRepository = ChatRepository(getInstance()!!)

    /** unimp小程序实例缓存 */
    var mUniMPCaches: HashMap<String, IUniMP> = HashMap()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUniappMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        mHandler = Handler()

//        val webSettings: WebSettings = binding.web.getSettings()
//        webSettings.javaScriptEnabled = true // 启用 JavaScript
//        webSettings.allowFileAccess = true // 允许访问文件
//        webSettings.allowFileAccessFromFileURLs = true // 允许从 file:// 访问文件
//        webSettings.allowUniversalAccessFromFileURLs = true // 允许从 file:// 访问所有文件
//        webSettings.setAllowUniversalAccessFromFileURLs(true);
//        binding.web.setWebViewClient(WebViewClient())
//        binding.web.setWebChromeClient(WebChromeClient())
//        binding.web.loadUrl("file:///android_asset/apps/web/test.html") // 修改后的 URL

        initData()
    }

    private fun initData() {

        binding.btn.setOnClickListener {
            try {
                val uniMPOpenConfiguration = UniMPOpenConfiguration()
//                uniMPOpenConfiguration.splashClass = MySplashView::class.java
//                uniMPOpenConfiguration.extraData.put("darkmode", "light")
                val uniMP = DCUniMPSDK.getInstance()
                    .openUniMP(mContext, "__UNI__EF861E1", uniMPOpenConfiguration)
                mUniMPCaches[uniMP.appid] = uniMP
            } catch (e: Exception) {
                Log.e("xxxx","异常："+e.message)
                e.printStackTrace()
            }
        }

        //清空好友表
        binding.btn2.setOnClickListener {
            Thread {
                val bool = chatRepository.deleteAllFriends()
                runOnUiThread {
                    Toast.makeText(mContext, bool.toString(), Toast.LENGTH_SHORT).show()
                }
            }.start()
        }

        //清空消息表
        binding.btn3.setOnClickListener {
            Thread {
                val bool = chatRepository.deleteAllChats()
                runOnUiThread {
                    Toast.makeText(mContext, bool.toString(), Toast.LENGTH_SHORT).show()
                }
            }.start()
        }

        DCUniMPSDK.getInstance().setOnUniMPEventCallBack(object : IOnUniMPEventCallBack {
            override fun onUniMPEventReceive(
                appid: String?,
                event: String,
                data: Any?,
                callback: DCUniMPJSCallback
            ) {
                Log.i("cs", "onUniMPEventReceive    event=$event")
                //回传数据给小程序
                callback.invoke("收到消息")
            }
        })

        checkPermission()


    }

    private fun signal(){
//        val identityKeyPair = KeyHelper.generateIdentityKeyPair()
//        val identityKey: IdentityKey = identityKeyPair.
//        val identityPublicKey = identityKey.getPublicKey().serialize()

    }

//    /**
//     * 模拟更新wgt
//     */
//    private fun updateWgt() {
//        // 替换真实的下载地址
//        val wgtUrl = "https://XXXXXXXX/unimp-sdk/__UNI__7AEA00D.wgt"
//        val wgtName = "__UNI__7AEA00D.wgt"
//
//        val downFilePath = externalCacheDir!!.path
//
//        val uiHandler = Handler()
//
//
//        DownloadUtil.get().download(
//            this@UniAPPMainActivity,
//            wgtUrl,
//            downFilePath,
//            wgtName,
//            object : OnDownloadListener() {
//                override fun onDownloadSuccess(file: File) {
//                    val uniMPReleaseConfiguration = UniMPReleaseConfiguration()
//                    uniMPReleaseConfiguration.wgtPath = file.path
//                    // 没有密码可以不写
//                    uniMPReleaseConfiguration.password = "789456123"
//
//                    uiHandler.post {
//                        DCUniMPSDK.getInstance().releaseWgtToRunPath(
//                            "__UNI__7AEA00D", uniMPReleaseConfiguration
//                        ) { code, pArgs ->
//                            if (code == 1) {
//                                //释放wgt完成
//                                try {
//                                    val uniMPOpenConfiguration = UniMPOpenConfiguration()
//                                    uniMPOpenConfiguration.extraData.put("darkmode", "auto")
//                                    DCUniMPSDK.getInstance().openUniMP(
//                                        this@UniAPPMainActivity,
//                                        "__UNI__7AEA00D",
//                                        uniMPOpenConfiguration
//                                    )
//                                } catch (e: java.lang.Exception) {
//                                    e.printStackTrace()
//                                }
//                            } else {
//                                //释放wgt失败
//                            }
//                        }
//                    }
//                }
//
//                override fun onDownloading(progress: Int) {
//                }
//
//                override fun onDownloadFailed() {
//                    Log.e("unimp", "downFilePath  ===  onDownloadFailed")
//                }
//            })
//    }


    /**
     * 检查并申请权限
     */
    fun checkPermission() {
        var targetSdkVersion = 0
        val PermissionString = arrayOf<String>(Manifest.permission.READ_PHONE_STATE,Manifest.permission.CHANGE_NETWORK_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE)
        try {
            val info = this.packageManager.getPackageInfo(this.packageName, 0)
            targetSdkVersion = info.applicationInfo!!.targetSdkVersion //获取应用的Target版本
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Build.VERSION.SDK_INT是获取当前手机版本 Build.VERSION_CODES.M为6.0系统
            //如果系统>=6.0
            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                //第 1 步: 检查是否有相应的权限
                val isAllGranted = checkPermissionAllGranted(PermissionString)
                if (isAllGranted) {
                    Log.e("err", "所有权限已经授权！")
                    return
                }
                // 一次请求多个权限, 如果其他有权限是已经授予的将会自动忽略掉
                ActivityCompat.requestPermissions(this, PermissionString, 1)
            }
        }
    }

    /**
     * 检查是否拥有指定的所有权限
     */
    private fun checkPermissionAllGranted(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 只要有一个权限没有被授予, 则直接返回 false
                //Log.e("err","权限"+permission+"没有授权");
                return false
            }
        }
        return true
    }
}