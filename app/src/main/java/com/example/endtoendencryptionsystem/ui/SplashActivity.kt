package com.example.endtoendencryptionsystem.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import autodispose2.androidx.lifecycle.autoDispose
import com.drake.statusbar.immersive
import com.example.endtoendencryptionsystem.databinding.ActivitySplashBinding
import com.example.endtoendencryptionsystem.entiy.vo.LoginVO
import com.example.endtoendencryptionsystem.http.Config
import com.example.endtoendencryptionsystem.repository.FriendRepository
import com.example.endtoendencryptionsystem.service.WebSocketService
import com.example.endtoendencryptionsystem.utils.json
import com.example.endtoendencryptionsystem.utils.toFriend
import com.example.endtoendencryptionsystem.utils.toJSONString
import com.example.endtoendencryptionsystem.viewmodel.SessionViewModel
import com.tencent.mmkv.MMKV
import com.wumingtech.at.http.ApiFactory
import com.wumingtech.at.viewmodel.factory.SessionViewModelFactory
import decodeParcelableCompat
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 6.19上午遇到的问题：以下逻辑需要完善
 1，在好友信息页面点击发送消息，如果是对话不存在的话，终止了。修复：在对话不存在时创建对话，但此时返回会导致对话列表页显示一条空对话。
 2，启动时同步好友信息有意义吗？除非是换设备后同步数据，好友表一般不会变，变的是user表，所以更需要同步的是user里面的信息。
 3，由于问题2的同步好友，后一系列的逻辑操作，目前有的数据导致好友对话的session不存在，发送不了消息。

 */

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun refreshTokenAndGoToMain(refreshToken: String) {
        ApiFactory.API.api.refreshToken(refreshToken)
            .flatMap {
                MMKV.defaultMMKV().encode("loginInfo", it)
                MMKV.defaultMMKV().encode("accessToken", it.accessToken)
                val fetchSelf = ApiFactory.API.api.getMyInfo().flatMap users@{
                    //保存个人信息
                    MMKV.defaultMMKV().encode("selfInfo", it)
                    MMKV.defaultMMKV().encode("userId",it.id)
                    return@users Flowable.just(true)
                }.doOnError{
                    Log.e("xxxx","同步个人异常："+it.message)
                }
//                val fetchFriends = ApiFactory.API.api.getFriends().flatMap friends@{
//                    //保存好友信息
//                    val friendRepository = FriendRepository(application)
//                    var userIds = arrayListOf<Int>()
//                    for (friendVO in it) {
//                        friendRepository.saveAndUpdateSession(friendVO.toFriend())
//                        userIds.add(friendVO.id.toInt())
//                    }
//                    //获取在线状态
//                    val fetchOnlineStatus = ApiFactory.API.api.fetchOlineStatus(userIds.joinToString(separator = ","))
//                        .flatMap onlineStatus@{
//                            // todo: 保存好友在线状态
//                            return@onlineStatus Flowable.just(true)
//                        }.doOnError{
//                            Log.e("xxxx","同步在线状态异常："+it.message)
//                        }
//                    return@friends fetchOnlineStatus
//                }.doOnError{
//                    Log.e("xxxx","同步好友异常："+it.message)
//                }
                val fetchGroups = ApiFactory.API.api.getGroups().flatMap groups@{
                    // todo: 保存群聊信息
                    return@groups Flowable.just(true)
                }.doOnError{
                    Log.e("xxxx","同步群组异常："+it.message)
                }
                return@flatMap Flowable.zip(fetchSelf, fetchGroups) { f1, f2 ->
                    if (f1 && f2 ) {
                        return@zip true
                    } else {
                        Log.e("xxxx","同步数据异常")
                        throw Throwable("同步数据异常")
                    }
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(this)
            .subscribe({
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }, {})

    }
}