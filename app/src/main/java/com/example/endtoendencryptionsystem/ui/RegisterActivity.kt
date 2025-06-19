package com.example.endtoendencryptionsystem.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider
import autodispose2.androidx.lifecycle.autoDispose
import autodispose2.autoDispose
import com.drake.statusbar.immersive
import com.example.endtoendencryptionsystem.databinding.ActivityLoginNamePwdBinding
import com.example.endtoendencryptionsystem.databinding.ActivityRegNamePwdBinding
import com.example.endtoendencryptionsystem.entiy.dto.LoginDTO
import com.example.endtoendencryptionsystem.entiy.dto.RegisterDTO
import com.example.endtoendencryptionsystem.entiy.vo.LoginVO
import com.example.endtoendencryptionsystem.http.RxSchedulers
import com.example.endtoendencryptionsystem.utils.EncryptionUtil
import com.example.endtoendencryptionsystem.utils.SignalKeyManager
import com.example.endtoendencryptionsystem.utils.ValidateUtil
import com.example.endtoendencryptionsystem.utils.json
import com.example.endtoendencryptionsystem.utils.toJSONString
import com.example.endtoendencryptionsystem.viewmodel.SessionViewModel
import com.jakewharton.rxbinding4.view.clicks
import com.ruins.library.sweet.SweetAlertDialog
import com.ruins.library.sweet.SweetAlertType
import com.tencent.mmkv.MMKV
import com.wumingtech.at.handler.handleGlobalError
import com.wumingtech.at.http.ApiFactory
import com.wumingtech.at.viewmodel.factory.SessionViewModelFactory
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.functions.Predicate
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegNamePwdBinding
    private val mContext: Context = this@RegisterActivity

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegNamePwdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersive()

        val viewModel: SessionViewModel by lazy { SessionViewModelFactory(application).create(SessionViewModel::class.java) }
        binding.btnNext.clicks().toFlowable(BackpressureStrategy.ERROR)
            .throttleFirst(1, TimeUnit.SECONDS)
            .filter { validator() }
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.io())
            .flatMap {
                val registerDTO = RegisterDTO(binding.etPhone.text.toString().trim(),
                    binding.etUsername.text.toString().trim(),
                    binding.etNickname.text.toString().trim(),
                    binding.etPwd.text.toString())
                    ApiFactory.API.api.register(registerDTO)
            }
            .compose(RxSchedulers.ioToMain())
            .retry(Predicate {
                binding.btnNext.text = "注   册"
                SweetAlertDialog(mContext, SweetAlertType.ERROR_TYPE)
                    .setContentText("注册失败：$it 请联系客服")
                    .setConfirmText("确认")
                    .show()
                return@Predicate true
            })
            .flatMap {
                ApiFactory.API.api.login(LoginDTO(binding.etPhone.text.toString().trim(),
                    binding.etPwd.text.toString(), 1))
            }.retry(Predicate {
                SweetAlertDialog(mContext, SweetAlertType.ERROR_TYPE)
                    .setContentText("自动登录失败，请重新登录")
                    .setConfirmText("确认")
                    .setConfirmClickListener {
                        it.dismissWithAnimation()
                        startActivity(Intent(mContext, LoginActivity::class.java))
                        finish()
                    }
                    .show()
                return@Predicate true
            })
            .flatMap { loginVO ->
                // 登录成功后获取个人信息
                MMKV.defaultMMKV().encode("loginInfo", loginVO)
                MMKV.defaultMMKV().encode("accessToken", loginVO.accessToken)
                ApiFactory.API.api.getMyInfo()
            }.flatMap { userVO ->
                //保存个人信息
                MMKV.defaultMMKV().encode("selfInfo", userVO)
                MMKV.defaultMMKV().encode("userId",userVO.id)
                // 生成密钥对并上传公钥
                val keyJson = EncryptionUtil.registerKey()
                // 上传公钥信息
                ApiFactory.API.api.updatePublicKeyInfo(keyJson)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(handleGlobalError(mContext))
            .autoDispose(AndroidLifecycleScopeProvider.from(this))
            .subscribe {
               if(it){
                   startActivity(Intent(mContext, MainActivity::class.java))
                   finish()
               }
            }

    }

    /**
     * 验证用户名、密码是否为空
     */
    private fun validator(): Boolean {
        Log.e("xxx","----------验证")
        return when {
            TextUtils.isEmpty(binding.etPhone.text.toString()) -> {
                binding.etUsername.error = "手机号不能为空"
                false
            }

            !ValidateUtil.isValidChinesePhone(binding.etPhone.text.toString())-> {
                binding.etPhone.error = "手机号格式错误"
                false
            }

            TextUtils.isEmpty(binding.etUsername.text.toString()) -> {
                binding.etUsername.error = "用户名不能为空"
                false
            }

            TextUtils.isEmpty(binding.etPwd.text.toString()) -> {
                binding.etPwd.error = "密码不能为空"
                false
            }

            else -> {
                binding.btnNext.text = "注册中..."
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


