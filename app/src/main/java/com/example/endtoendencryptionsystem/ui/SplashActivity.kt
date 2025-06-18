package com.example.endtoendencryptionsystem.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import autodispose2.androidx.lifecycle.autoDispose
import com.drake.statusbar.immersive
import com.example.endtoendencryptionsystem.databinding.ActivitySplashBinding
import com.example.endtoendencryptionsystem.entiy.vo.LoginVO
import com.example.endtoendencryptionsystem.http.Config
import com.example.endtoendencryptionsystem.service.WebSocketService
import com.example.endtoendencryptionsystem.utils.json
import com.example.endtoendencryptionsystem.utils.toJSONString
import com.example.endtoendencryptionsystem.viewmodel.SessionViewModel
import com.tencent.mmkv.MMKV
import com.wumingtech.at.http.ApiFactory
import com.wumingtech.at.viewmodel.factory.SessionViewModelFactory
import decodeParcelableCompat
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers


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
            Log.e("xxx", "loginInfo:" + json.toJSONString(loginInfo))
            // 已登录，刷新token
            refreshTokenAndGoToMain(loginInfo.refreshToken)
        } else {
            // 未登录，跳转到登录页
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
        }
    }

    private fun refreshTokenAndGoToMain(refreshToken: String) {
        ApiFactory.API.api.refreshToken(refreshToken)
            .flatMap {
                MMKV.defaultMMKV().encode("loginInfo", it)
                val fetchFriends = ApiFactory.API.api.getFriends().flatMap friends@{
                    // todo: 保存好友信息
                    val fetchOnlineStatus = ApiFactory.API.api.fetchOlineStatus("")
                        .flatMap onlineStatus@{
                            // todo: 保存好友在线状态
                            return@onlineStatus Flowable.just(true)
                        }
                    return@friends fetchOnlineStatus
                }
                val fetchGroups = ApiFactory.API.api.getGroups().flatMap groups@{
                    // todo: 保存群聊信息
                    return@groups Flowable.just(true)
                }
                return@flatMap Flowable.zip(fetchFriends, fetchGroups) { _, _ ->
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(this)
            .subscribe({
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
                val loginInfo: LoginVO? = MMKV.defaultMMKV().decodeParcelableCompat<LoginVO>("loginInfo")
                loginInfo?.accessToken?.let { accessToken ->
                    initWebsocketClient(accessToken)
                }
            }, {})

    }

    private fun initWebsocketClient(accessToken: String) {
        val intent = Intent(this, WebSocketService::class.java).apply {
            putExtra("wsUrl", Config.receiveMessageURL)
            putExtra("token", accessToken)
        }
        Log.d("SplashActivity", "启动service")
        startService(intent)

    }
}