package com.bc.wechat.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.endtoendencryptionsystem.R
import com.example.endtoendencryptionsystem.adapter.FriendAdapter
import com.example.endtoendencryptionsystem.adapter.GroupedFriendAdapter

import com.example.endtoendencryptionsystem.databinding.FragmentContactsBinding
import com.example.endtoendencryptionsystem.entiy.FriendItem
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.User
import com.example.endtoendencryptionsystem.ui.AddFriendsActivity
import com.example.endtoendencryptionsystem.ui.FriendInfoActivity
import com.example.endtoendencryptionsystem.utils.json
import com.example.endtoendencryptionsystem.utils.toJSONString
import com.example.endtoendencryptionsystem.viewmodel.ChatViewModel
import com.example.endtoendencryptionsystem.viewmodel.FriendViewModel
import java.util.Date

import kotlin.getValue
import kotlin.jvm.java

/**
 * 好友
 */
class ContactsFragment : Fragment() {
    private lateinit var binding: FragmentContactsBinding
    private val vm by activityViewModels<FriendViewModel>()
    private lateinit var mFriendsAdapter: GroupedFriendAdapter
    private var mFriendList = ArrayList<Friend>()
    private var datas = ArrayList<FriendItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactsBinding.inflate(layoutInflater)
        initView()
        return binding.root
    }

    private fun initView() {
        binding.layoutTitle.tvTitle.text = "好友"
        binding.layoutTitle.ivBack.visibility = View.GONE
        mFriendsAdapter = GroupedFriendAdapter(datas)
        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        binding.rv.adapter = mFriendsAdapter
        vm.getFriends()
        vm.getGroupedFriendList()
        observeData()

        //添加好友
        binding.layoutTitle.ivAdd.setOnClickListener {
            val intent = Intent(requireContext(), AddFriendsActivity::class.java)
            startActivity(intent)
        }
        //搜索好友
        binding.layoutTitle.ivSearch.setOnClickListener {  }

        mFriendsAdapter.setOnItemClickListener { adapter, view, position ->
            val friend = (mFriendsAdapter.data[position] as FriendItem.FriendEntry).friend
            var intent = Intent(requireContext(), FriendInfoActivity::class.java)
            intent.putExtra("friendInfo", friend)
            startActivity(intent)
        }

    }

    /**
     * 监听数据
     */
    fun observeData(){
        //TODO  bug:新增好友后，该监听没收到（确定走vm.getGroupedFriendList了）
        vm.friendList.observe(viewLifecycleOwner) {
            mFriendsAdapter.setList(it)
        }

        //同步好友列表
        vm.syncFriendResult.observe(viewLifecycleOwner){
            Log.e("xxx","走这儿吗")
            vm.getGroupedFriendList()
        }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }


    fun groupByFirstLetter(list: List<Friend>): Map<Char, List<Friend>> {
        return list.groupBy { it.friendNickName!!.first() }
    }

}