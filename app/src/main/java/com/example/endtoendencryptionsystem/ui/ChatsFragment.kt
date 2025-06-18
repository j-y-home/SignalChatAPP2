package com.example.endtoendencryptionsystem.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.endtoendencryptionsystem.adapter.ConverssationAdapter
import com.example.endtoendencryptionsystem.adapter.GroupedFriendAdapter
import com.example.endtoendencryptionsystem.databinding.FragmentConversationBinding
import com.example.endtoendencryptionsystem.entiy.FriendItem
import com.example.endtoendencryptionsystem.entiy.database.ChatConversation
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.utils.json
import com.example.endtoendencryptionsystem.utils.toJSONString
import com.example.endtoendencryptionsystem.viewmodel.ChatViewModel
import com.example.endtoendencryptionsystem.viewmodel.FriendViewModel


/**
 * 消息对话
 */
class ChatsFragment : Fragment() {
    private lateinit var binding: FragmentConversationBinding
    private val vm by activityViewModels<ChatViewModel>()
    private val vmFriend by activityViewModels<FriendViewModel>()
    private lateinit var adapter: ConverssationAdapter
    private var datas = ArrayList<ChatConversation>()
    private var currentConId:Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConversationBinding.inflate(layoutInflater)
        initView()
        getData()
        obServer()
        return binding.root
    }

    private fun initView() {
        binding.layoutTitle.tvTitle.text = "消息"
        binding.layoutTitle.ivBack.visibility = View.GONE

    }

    private fun getData() {
        adapter = ConverssationAdapter(datas)
        binding.rvConversation.layoutManager = LinearLayoutManager(requireContext())
        binding.rvConversation.adapter = adapter
        vm.getAllConversations()

        //对话点击
        adapter.setOnItemClickListener { _, _, position ->
            if(datas[position].type == "PRIVATE"){
                currentConId = datas[position].id
                vmFriend.getFriendByFriendId(datas[position].targetId)
            }else{
                //TODO 群聊

            }
        }
    }

    private fun obServer() {
        vm.getAllConversations.observe(viewLifecycleOwner) {
            Log.e("xxx","验证这里数据库表变，会自动更新："+ json.toJSONString(it))
            datas.clear()
            datas.addAll(it)
            adapter.setList(datas)
        }

        vmFriend.friendInfo.observe(viewLifecycleOwner) {
            var intent = Intent(requireContext(), PrivateChatMsgActivity::class.java)
            intent.putExtra("friendInfo",it)
            intent.putExtra("conversationId",currentConId)
            startActivity(intent)
        }


    }





}
