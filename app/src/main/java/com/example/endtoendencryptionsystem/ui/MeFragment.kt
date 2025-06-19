package com.example.endtoendencryptionsystem.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.endtoendencryptionsystem.databinding.FragmentConversationBinding

import com.example.endtoendencryptionsystem.databinding.FragmentMeBinding
import com.example.endtoendencryptionsystem.entiy.database.User
import com.example.endtoendencryptionsystem.service.WebSocketService
import com.ruins.library.sweet.SweetAlertDialog
import com.ruins.library.sweet.SweetAlertType
import com.tencent.mmkv.MMKV


/**
 * tab - "我"
 */
class MeFragment : Fragment(){
    private lateinit var binding: FragmentMeBinding
    private lateinit var mUser: User

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMeBinding.inflate(layoutInflater)
        initView()
        return binding.root
    }

    private fun initView() {
        binding.layoutTitle.tvTitle.text = "我的"
        binding.layoutTitle.ivBack.visibility = View.GONE

        binding.tvQuit.setOnClickListener {
            /**
             * 退出登录，Uniapp做的操作：
             * 1，删除缓存
             * 2，websocketService关闭
             * 3，跳转到登录页
             * 4，删除数据库信息？
             */
            SweetAlertDialog(requireContext(), SweetAlertType.WARNING_TYPE)
                .setContentText("您确定要退出登录吗？")
                .setConfirmText("确定")
                .setConfirmClickListener {
                    it.dismissWithAnimation()
                    //1，清除缓存
                    MMKV.defaultMMKV().clearAll()
                    //2，关闭websocketService
                    val intent = Intent(requireContext(), WebSocketService::class.java)
                    requireContext().stopService(intent)
                    //3，跳转到登录页
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finish()
                }.setCancelButton("取消") {
                    it.dismissWithAnimation()
                }
                .show()
        }
    }



}
