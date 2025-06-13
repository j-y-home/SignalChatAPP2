package com.example.endtoendencryptionsystem.ui


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.activity.viewModels

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.drake.statusbar.immersive
import com.example.endtoendencryptionsystem.R
import com.example.endtoendencryptionsystem.adapter.FriendsAdapter
import com.example.endtoendencryptionsystem.adapter.GroupedFriendAdapter
import com.example.endtoendencryptionsystem.databinding.ActivityAddFriendBinding
import com.example.endtoendencryptionsystem.databinding.ActivityMainBinding
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.User
import com.example.endtoendencryptionsystem.viewmodel.FriendViewModel

import com.lnsoft.conslutationsystem.core.SPConfig
import com.tencent.mmkv.MMKV
import kotlin.getValue


/**
 * 添加好友
 */
class AddFriendsActivity : AppCompatActivity(){
    private lateinit var binding: ActivityAddFriendBinding
    private val viewModel by viewModels<FriendViewModel>()
    private lateinit var adapter: FriendsAdapter
    private var data = ArrayList<User>()
    //用户的好友列表
    private var userFriends = ArrayList<Friend>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFriendBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersive()
        initView()

       // getLocalFriends()
    }

    private fun getLocalFriends() {
        viewModel.getUserFriendList(MMKV.defaultMMKV().decodeInt("userId"))
        viewModel.userFriendsList.observe(this) {
            userFriends.clear()
            userFriends.addAll(it)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun initView() {
        binding.layoutTitle.tvTitle.text = "添加好友"
        binding.layoutTitle.ivBack.setOnClickListener { finish() }
        binding.tvCancel.setOnClickListener { finish() }


        adapter = FriendsAdapter(data)
        binding.rv.layoutManager = LinearLayoutManager(this)
        binding.rv.adapter = adapter
        adapter.addChildClickViewIds(R.id.tv_add)

        //加为好友
        adapter.setOnItemChildClickListener { _, view, position ->
            if(view.id == R.id.tv_add){
                val friend = Friend(
                    userId = MMKV.defaultMMKV().decodeInt("userId"),
                    friendId = data[position].id,
                    friendNickName = data[position].nickName,
                    friendHeadImage = data[position].headImage,
                    preKeyBundleMaker = data[position].preKeyBundleMaker
                )
                viewModel.addFriend(friend)
            }
        }
        //如何监听edittext输入键盘的搜索按钮事件
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                Log.e("xxx","搜索事件")
                val key = binding.etSearch.text.toString()
                if (key.isNotEmpty()) {
                    viewModel.getSearchUserListByKey(key)
                }
            }
            true
        }
        binding.etSearch.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                Log.e("xxx","回车事件")
                val key = binding.etSearch.text.toString()
                if (key.isNotEmpty()) {
                    viewModel.getSearchUserListByKey(key)
                }
                true
            } else {
                false
            }
        }

        viewModel.searchUsersList.observe(this) {
            data.clear()
            data.addAll(it)
            adapter.notifyDataSetChanged()
        }
        viewModel.addFriendResult.observe(this) {
            if (it) {
                Log.e("xxx","添加好友成功")
                //刷新fragment页面的好友列表
                viewModel.getGroupedFriendList()
                finish()
            }
        }

    }




}