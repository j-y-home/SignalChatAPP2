//package com.example.endtoendencryptionsystem.ui
//
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.text.TextUtils
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.fragment.app.Fragment
//import butterknife.BindView
//import butterknife.ButterKnife
//import butterknife.OnClick
//import com.bc.wechat.R
//import com.bc.wechat.activity.BigImageActivity
//import com.bc.wechat.activity.MyProfileActivity
//import com.bc.wechat.activity.SettingActivity
//import com.bc.wechat.activity.StatusActivity
//import com.bc.wechat.databinding.FragmentConversationBinding
//import com.bc.wechat.databinding.FragmentMeBinding
//import com.bc.wechat.entity.User
//import com.bc.wechat.utils.OssUtil
//import com.bc.wechat.utils.PreferencesUtil
//import com.facebook.drawee.view.SimpleDraweeView
//
///**
// * tab - "我"
// */
//class MeFragment : Fragment(){
//    private lateinit var binding: FragmentMeBinding
//    private lateinit var mUser: User
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_me, container, false)
//        ButterKnife.bind(this, view)
//        return view
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        mUser = PreferencesUtil.getInstance().user
//        initView()
//    }
//
//    private fun initView() {
//        binding.tvName.text = mUser!!.userNickName
//        val userWxId = if (mUser!!.userWxId == null) "" else mUser!!.userWxId
//        binding.tvWxId.text = "微信号:$userWxId"
//        val userAvatar = mUser!!.userAvatar
//        if (!TextUtils.isEmpty(userAvatar)) {
//            val resizeAvatarUrl = OssUtil.resize(mUser!!.userAvatar)
//            binding.sdvAvatar.setImageURI(Uri.parse(resizeAvatarUrl))
//        }
//
//        binding.rlMe.setOnClickListener {
//            startActivity(Intent(activity, MyProfileActivity::class.java))
//        }
//        binding.rlStatus.setOnClickListener {
//            startActivity(Intent(activity, StatusActivity::class.java))
//        }
//        binding.rlSettings.setOnClickListener {
//            startActivity(Intent(activity, SettingActivity::class.java))
//        }
//        binding.sdvAvatar.setOnClickListener{
//            val intent = Intent(activity, BigImageActivity::class.java)
//            intent.putExtra("imgUrl", mUser!!.userAvatar)
//            startActivity(intent)
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        mUser = PreferencesUtil.getInstance().user
//        binding.tvName.text = mUser.getUserNickName()
//        val userWxId = if (mUser.getUserWxId() == null) "" else mUser.getUserWxId()
//        binding.tvWxId.text = "微信号:$userWxId"
//        if (!TextUtils.isEmpty(mUser.getUserAvatar())) {
//            val resizeAvatarUrl = OssUtil.resize(mUser.getUserAvatar())
//            binding.sdvAvatar.setImageURI(Uri.parse(resizeAvatarUrl))
//        }
//    }
//}
