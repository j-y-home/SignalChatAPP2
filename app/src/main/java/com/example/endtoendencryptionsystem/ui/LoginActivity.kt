//package com.example.endtoendencryptionsystem.ui
//
//import android.content.Intent
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.view.View
//import com.example.endtoendencryptionsystem.R
//import com.example.endtoendencryptionsystem.core.BaseActivity
//import com.example.endtoendencryptionsystem.databinding.ActivityLoginBinding
//import com.example.endtoendencryptionsystem.utils.ValidateUtil
//import com.example.endtoendencryptionsystem.widget.LoadingDialog
//import com.lnsoft.conslutationsystem.core.Config
//import com.therouter.TheRouter
//
///**
// * 登录
// *
// * @author zhou
// */
//class LoginActivity : BaseActivity() {
//    private lateinit var binding: ActivityLoginBinding
//    var mDialog: LoadingDialog? = null
//    private var mLoginType = Config.LOGIN_TYPE_PHONE_AND_PASSWORD
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityLoginBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        TheRouter.inject(this)
//        initView()
//        initListener()
//    }
//
//    fun initView() {
//        mDialog = LoadingDialog(this@LoginActivity)
//    }
//
//    fun initListener() {
//        binding.etPhone.addTextChangedListener(TextChange())
//        binding.etAccount.addTextChangedListener(TextChange())
//        binding.etPassword.addTextChangedListener(TextChange())
//        binding.tvLoginType.setOnClickListener {
//            // 手机号登录 or 其他账号(微信号/QQ号/邮箱)登录
//            if (Config.LOGIN_TYPE_PHONE_AND_PASSWORD == mLoginType) {
//                // 当前登录方式手机号登录
//                // 切换为其他账号登录
//                binding.llLoginViaWechatIdEmailQqId.visibility = View.VISIBLE
//                binding.llLoginViaMobileNumber.visibility = View.GONE
//                mLoginType = Config.LOGIN_TYPE_OTHER_ACCOUNTS_AND_PASSWORD
//                binding.etPhone.setText("")
//                binding.tvTitle.text = getString(R.string.login_via_wechat_id_email_qq_id)
//                binding.tvLoginType.text = getString(R.string.use_mobile_number_to_login)
//                binding.btnNext.text = getString(R.string.login)
//            } else {
//                // 当前登录方式其他账号登录
//                // 切换为手机号登录
//                binding.llLoginViaMobileNumber.visibility = View.VISIBLE
//                binding.llLoginViaWechatIdEmailQqId.visibility = View.GONE
//                mLoginType = Config.LOGIN_TYPE_PHONE_AND_PASSWORD
//                binding.etAccount.setText("")
//                binding.etPassword.setText("")
//                binding.tvTitle.text = getString(R.string.login_via_mobile_number)
//                binding.tvLoginType.text = getString(R.string.use_wechat_id_email_qq_id_to_login)
//                binding.btnNext.text = getString(R.string.next)
//            }
//        }
//
//        binding.btnNext.setOnClickListener {
//            mDialog!!.setMessage(getString(R.string.please_wait))
//            mDialog!!.setCanceledOnTouchOutside(false)
//            mDialog!!.show()
//
//            val phone = binding.etPhone.text.toString()
//
//            // 是否有效手机号
//            val isValidChinesePhone = ValidateUtil.isValidChinesePhone(phone)
//            mDialog!!.dismiss()
//            if (isValidChinesePhone) {
////                // 有效 TODO
////                val intent = Intent(
////                    this@LoginActivity,
////                    PhoneLoginFinalActivity::class.java
////                )
////                intent.putExtra("phone", phone)
////                startActivity(intent)
//                startActivity(Intent(this@LoginActivity,MainActivity::class.java))
//            } else {
//                // 无效
//                showAlertDialog(
//                    this@LoginActivity, "手机号码错误",
//                    "你输入的是一个无效的手机号码",
//                    "确定", true
//                )
//            }
//        }
//    }
//
//
//
//    fun back(view: View?) {
//        finish()
//    }
//
//    internal inner class TextChange : TextWatcher {
//        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
//        }
//
//        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
//            if (Config.LOGIN_TYPE_PHONE_AND_PASSWORD == mLoginType) {
//                // 手机号登录
//                val phoneEtHasText = binding.etPhone.text.length > 0
//                if (phoneEtHasText) {
//                    // "下一步"按钮可用
//                    binding.btnNext.setBackgroundResource(R.drawable.btn_login_next_enable)
//                    binding.btnNext.setTextColor(getColor(R.color.register_btn_text_enable))
//                    binding.btnNext.isEnabled = true
//                } else {
//                    // "下一步"按钮不可用
//                    binding.btnNext.setBackgroundResource(R.drawable.btn_login_next_disable)
//                    binding.btnNext.setTextColor(getColor(R.color.register_btn_text_disable))
//                    binding.btnNext.isEnabled = false
//                }
//            } else {
//                // 其他账号登录
//                val accountEtHasText = binding.etAccount.text.length > 0
//                val passwordEtHasText = binding.etPassword.text.length > 0
//                if (accountEtHasText && passwordEtHasText) {
//                    // "登录"按钮可用
//                    binding.btnNext.setBackgroundResource(R.drawable.btn_login_next_enable)
//                    binding.btnNext.setTextColor(getColor(R.color.register_btn_text_enable))
//                    binding.btnNext.isEnabled = true
//                } else {
//                    // "登录"按钮不可用
//                    binding.btnNext.setBackgroundResource(R.drawable.btn_login_next_disable)
//                    binding.btnNext.setTextColor(getColor(R.color.register_btn_text_disable))
//                    binding.btnNext.isEnabled = false
//                }
//            }
//        }
//
//        override fun afterTextChanged(editable: Editable) {
//        }
//    }
//}