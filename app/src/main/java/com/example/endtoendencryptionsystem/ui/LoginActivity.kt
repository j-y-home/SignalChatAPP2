package com.example.endtoendencryptionsystem.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider
import autodispose2.androidx.lifecycle.autoDispose
import autodispose2.autoDispose
import com.drake.statusbar.immersive
import com.example.endtoendencryptionsystem.databinding.ActivityLoginNamePwdBinding
import com.example.endtoendencryptionsystem.entiy.vo.LoginVO
import com.example.endtoendencryptionsystem.utils.json
import com.example.endtoendencryptionsystem.utils.toJSONString
import com.example.endtoendencryptionsystem.viewmodel.SessionViewModel
import com.jakewharton.rxbinding4.view.clicks
import com.tencent.mmkv.MMKV
import com.wumingtech.at.handler.handleGlobalError
import com.wumingtech.at.viewmodel.factory.SessionViewModelFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.functions.Predicate
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginNamePwdBinding
    private val mContext: Context = this@LoginActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginNamePwdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersive()
        val viewModel: SessionViewModel by lazy { SessionViewModelFactory(application).create(SessionViewModel::class.java) }
        binding.btnNext.clicks().toFlowable(BackpressureStrategy.ERROR)
            .throttleFirst(1, TimeUnit.SECONDS)
            .filter { validator() }
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.io())
            .flatMap {
                return@flatMap viewModel.createSession(
                    binding.etUsername.text.toString().trim(),
                    binding.etPwd.text.toString().trim()
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(handleGlobalError(mContext))
            .autoDispose(AndroidLifecycleScopeProvider.from(this))
            .subscribe( {
                Log.e("xxxx","登录成功："+json.toJSONString(it))
                val loginInfo : LoginVO =  it
                MMKV.defaultMMKV().encode("loginInfo",it)
                MMKV.defaultMMKV().encode("accessToken", loginInfo.accessToken)
                MMKV.defaultMMKV().encode("userId", loginInfo.userId.toInt())
                MMKV.defaultMMKV().encode("userName", binding.etUsername.text.toString().trim())
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            },{
                binding.btnNext.text = "登录"
                Log.e("xxxx","登录失败："+it.message)
                Toast.makeText(mContext, "登录失败："+it.message, Toast.LENGTH_SHORT).show()
            })
    }

    /**
     * 验证用户名、密码是否为空
     */
    private fun validator(): Boolean {
        Log.e("xxx","----------验证")
        return when {
            TextUtils.isEmpty(binding.etUsername.text.toString()) -> {
                binding.etUsername.error = "用户名不能为空"
                false
            }

            TextUtils.isEmpty(binding.etPwd.text.toString()) -> {
                binding.etPwd.error = "密码不能为空"
                false
            }

            else -> {
                binding.btnNext.text = "登录中..."
                true
            }
        }
    }

    /**
     * 重写dispatchTouchEvent
     */
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v!!.windowToken)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     *
     * @param v
     * @param event
     * @return false
     */
    private fun isShouldHideKeyboard(v: View?, event: MotionEvent): Boolean {
        if (v != null && v is EditText) {
            val l = intArrayOf(0, 0)
            v.getLocationInWindow(l)
            val left = l[0]
            val top = l[1]
            val bottom = top + v.height
            val right = left + v.width
            return !(event.x > left && event.x < right && event.y > top && event.y < bottom)
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     * @param token
     */
    @SuppressLint("WrongConstant")
    private fun hideKeyboard(token: IBinder?) {
        if (token != null) {
            val im = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}


