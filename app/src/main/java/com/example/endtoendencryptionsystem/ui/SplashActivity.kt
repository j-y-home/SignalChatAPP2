package com.example.endtoendencryptionsystem.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.drake.statusbar.immersive
import com.example.endtoendencryptionsystem.databinding.ActivitySplashBinding
import com.example.endtoendencryptionsystem.entiy.vo.LoginVO
import com.example.endtoendencryptionsystem.http.Config
import com.example.endtoendencryptionsystem.service.WebSocketService
import com.example.endtoendencryptionsystem.utils.json
import com.example.endtoendencryptionsystem.utils.toJSONString
import com.example.endtoendencryptionsystem.viewmodel.SessionViewModel
import com.tencent.mmkv.MMKV
import com.wumingtech.at.viewmodel.factory.SessionViewModelFactory
import decodeParcelableCompat


class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val mContext: Context = this@SplashActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersive()
      //  Handler().postDelayed(this::checkLoginStatus, 1000) // 延迟1秒模拟开屏动画
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        val loginInfo: LoginVO? = MMKV.defaultMMKV().decodeParcelableCompat<LoginVO>("loginInfo")
        if (loginInfo != null && loginInfo.refreshToken!!.isNotEmpty()) {
            Log.e("xxx","loginInfo:"+ json.toJSONString(loginInfo))
            // 已登录，刷新token
            refreshTokenAndGoToMain(loginInfo.refreshToken)
        } else {
            // 未登录，跳转到登录页
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
        }
    }

    private fun refreshTokenAndGoToMain(refreshToken: String) {
        val viewModel: SessionViewModel by lazy { SessionViewModelFactory(application).create(SessionViewModel::class.java) }
        viewModel.refreshToken(refreshToken)

        viewModel.refreshResult.observe(this) {
            MMKV.defaultMMKV().encode("loginInfo", it)
            startActivity(Intent(this@SplashActivity,  MainActivity::class.java))
            finish()
            initWebsocketClient(it.accessToken!!)
        }
    }

    private fun initWebsocketClient(accessToken:String){
        val intent = Intent(this, WebSocketService::class.java).apply {
            putExtra("wsUrl", Config.receiveMessageURL)
            putExtra("token", accessToken)
        }
        Log.d("SplashActivity","启动service")
        startService(intent)

    }
}