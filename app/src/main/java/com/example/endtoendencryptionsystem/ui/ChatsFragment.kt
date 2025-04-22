//package com.example.endtoendencryptionsystem.ui
//
//import android.content.Intent
//import android.os.Bundle
//import android.os.Handler
//import android.os.Message
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.AdapterView
//import androidx.fragment.app.Fragment
//import com.example.endtoendencryptionsystem.databinding.FragmentConversationBinding
//import com.example.endtoendencryptionsystem.entiy.Conversation
//
//
//class ChatsFragment : Fragment() {
//    private lateinit var binding: FragmentConversationBinding
//
//    var mConversationAdapter: ConversationAdapter? = null
//    var mConversationList: List<Conversation>? = null
//
//    var mUserDao: UserDao? = null
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding = FragmentConversationBinding.inflate(layoutInflater)
//        return binding.root
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//        setTitleStrokeWidth(binding.tvTitle)
//
//        mUserDao = UserDao()
//        mConversationList = JMessageClient.getConversationList()
//        if (null == mConversationList) {
//            mConversationList = ArrayList()
//        }
//        mConversationAdapter = ConversationAdapter(activity, mConversationList)
//        binding.lvConversation.adapter = mConversationAdapter
//
//        binding.lvConversation.onItemClickListener =
//            AdapterView.OnItemClickListener { parent, view, position, id ->
//                val conversation = mConversationList!![position]
//                var newMsgsUnreadNum =
//                    PreferencesUtil.getInstance().newMsgsUnreadNumber - conversation.unReadMsgCnt
//                if (newMsgsUnreadNum < 0) {
//                    newMsgsUnreadNum = 0
//                }
//                PreferencesUtil.getInstance().newMsgsUnreadNumber = newMsgsUnreadNum
//                // 清除未读
//                conversation.resetUnreadCount()
//                if (conversation.type == ConversationType.single) {
//                    val userInfo = conversation.targetInfo as UserInfo
//
//                    val user = mUserDao!!.getUserById(userInfo.userName)
//                    val intent = Intent(activity, ChatActivity::class.java)
//                    intent.putExtra("targetType", Constant.TARGET_TYPE_SINGLE)
//                    intent.putExtra("contactId", user.userId)
//                    intent.putExtra("contactNickName", user.userNickName)
//                    intent.putExtra("contactAvatar", user.userAvatar)
//                    startActivity(intent)
//                } else {
//                    val groupInfo = conversation.targetInfo as GroupInfo
//                    val intent = Intent(activity, ChatActivity::class.java)
//                    intent.putExtra("targetType", Constant.TARGET_TYPE_GROUP)
//                    intent.putExtra("groupId", groupInfo.groupID.toString())
//                    intent.putExtra("groupDesc", groupInfo.groupDescription)
//                    intent.putExtra("memberNum", groupInfo.groupMemberInfos.size.toString())
//                    startActivity(intent)
//                }
//            }
//    }
//
//    fun refreshConversationList() {
//        mHandler.sendMessage(mHandler.obtainMessage(REFRESH_CONVERSATION_LIST))
//    }
//
//    private val mHandler: Handler = object : Handler() {
//        override fun handleMessage(msg: Message) {
//            if (msg.what == REFRESH_CONVERSATION_LIST) {
//                val newConversationList = JMessageClient.getConversationList()
//                mConversationAdapter!!.setData(newConversationList)
//                mConversationAdapter!!.notifyDataSetChanged()
//            }
//        }
//    }
//
//    companion object {
//        private const val REFRESH_CONVERSATION_LIST = 0x3000
//    }
//}
