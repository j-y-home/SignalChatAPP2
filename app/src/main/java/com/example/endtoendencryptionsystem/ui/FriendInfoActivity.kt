package com.example.endtoendencryptionsystem.ui


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import autodispose2.androidx.lifecycle.autoDispose
import cn.pedant.SweetAlert.SweetAlertDialog
import com.drake.statusbar.immersive
import com.example.endtoendencryptionsystem.databinding.ActivityFriendInfoBinding
import com.example.endtoendencryptionsystem.databinding.ActivityMainBinding
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.viewmodel.FriendViewModel


import kotlin.getValue


/**
 * 好友信息
 */
class FriendInfoActivity : AppCompatActivity(){
    private lateinit var binding: ActivityFriendInfoBinding
    private val viewModel by viewModels<FriendViewModel>()

    lateinit var friendInfo: Friend

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersive()
        initView()
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("NotifyDataSetChanged")
    fun initView() {
        binding.layoutTitle.tvTitle.text = "用户信息"
        binding.layoutTitle.ivBack.setOnClickListener { finish() }
        binding.layoutTitle.ivAdd.visibility = View.GONE
        binding.layoutTitle.ivSearch.visibility = View.GONE

        friendInfo = intent.getParcelableExtra<Friend>("friendInfo")!!


        //发送消息
        binding.tvSendMsg.setOnClickListener {
            val intent = Intent(this, PrivateChatMsgActivity::class.java)
            intent.putExtra("friendInfo", friendInfo)
            startActivity(intent)
        }

        //删除好友
        binding.tvDel.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setContentText("您确定要删除该好友吗？")
                .setConfirmText("确定")
                .setConfirmClickListener {
                    viewModel.delFriend(friendInfo.friendId)
                    it.dismiss()
                }
                .show()
        }

        viewModel.delFriendResult.observe(this){
            if (it) {
                Toast.makeText(this, "与该好友关系已解除", Toast.LENGTH_SHORT).show()
                finish()
                //刷新列表
                viewModel.getGroupedFriendList()
            } else {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setContentText("删除失败")
                    .setConfirmText("确定")
                    .setConfirmClickListener {
                        it.dismiss()
                    }
                    .show()
            }
        }


    }




}