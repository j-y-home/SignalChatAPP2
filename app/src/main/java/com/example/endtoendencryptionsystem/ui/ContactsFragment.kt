//package com.bc.wechat.fragment
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.AdapterView
//import android.widget.ListView
//import android.widget.RelativeLayout
//import android.widget.TextView
//import androidx.fragment.app.Fragment
//import butterknife.BindView
//import butterknife.ButterKnife
//import com.bc.wechat.R
//import com.bc.wechat.activity.NewFriendsActivity
//import com.bc.wechat.activity.UserInfoActivity
//import com.bc.wechat.activity.UserInfoFileHelperActivity
//import com.bc.wechat.activity.UserInfoMyActivity
//import com.bc.wechat.adapter.FriendsAdapter
//import com.bc.wechat.cons.Constant
//import com.bc.wechat.dao.UserDao
//import com.bc.wechat.databinding.FragmentContactsBinding
//import com.bc.wechat.databinding.FragmentConversationBinding
//import com.bc.wechat.entity.User
//import com.bc.wechat.utils.PinyinComparator
//import com.bc.wechat.utils.PreferencesUtil
//import java.util.Collections
//
///**
// * 通讯录
// *
// */
//class ContactsFragment : Fragment() {
//    private lateinit var binding: FragmentContactsBinding
//    var mFriendsAdapter: FriendsAdapter? = null
//    private lateinit  var mInflater: LayoutInflater
//
//    // 好友总数
//    var mFriendsCountTv: TextView? = null
//
//    var mNewFriendsUnreadNumTv: TextView? = null
//
//    // 好友列表
//    private lateinit var mFriendList: List<User>
//
//    // 星标好友列表
//    private lateinit var mStarFriendList: MutableList<User>
//
//    var mUserDao: UserDao? = null
//    var mUser: User? = null
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_contacts, container, false)
//        ButterKnife.bind(this, view)
//        return view
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        PreferencesUtil.getInstance().init(activity)
//        mUserDao = UserDao()
//        mUser = PreferencesUtil.getInstance().user
//
//        setTitleStrokeWidth(binding.tvTitle)
//
//        mInflater = LayoutInflater.from(activity)
//        val headerView = mInflater.inflate(R.layout.item_contacts_header, null)
//        binding.lvFriends.addHeaderView(headerView)
//        val footerView = mInflater.inflate(R.layout.item_contacts_footer, null)
//        binding.lvFriends.addFooterView(footerView)
//
//        mFriendsCountTv = footerView.findViewById(R.id.tv_total)
//
//        val mNewFriendsRl = headerView.findViewById<RelativeLayout>(R.id.rl_new_friends)
//        mNewFriendsRl.setOnClickListener {
//            startActivity(Intent(activity, NewFriendsActivity::class.java))
//            PreferencesUtil.getInstance().setNewFriendsUnreadNumber(0)
//        }
//
//        val mGroupChatsRl = headerView.findViewById<RelativeLayout>(R.id.rl_group_chats)
//        mGroupChatsRl.setOnClickListener { }
//
//        mNewFriendsUnreadNumTv = headerView.findViewById(R.id.tv_new_friends_unread)
//
//        mStarFriendList = mUserDao!!.allStarredContactList
//        mFriendList = mUserDao!!.allFriendList
//        // 对list进行排序
//        Collections.sort(mFriendList, object : PinyinComparator() {
//        })
//
//        mStarFriendList.addAll(mFriendList)
//
//        mFriendsAdapter = FriendsAdapter(activity, R.layout.item_contacts, mStarFriendList)
//        binding.lvFriends.adapter = mFriendsAdapter
//
//        mFriendsCountTv.setText(mUserDao!!.contactsCount.toString() + "位联系人")
//
//        binding.lvFriends.onItemClickListener =
//            AdapterView.OnItemClickListener { parent, view, position, id ->
//                if (position != 0 && position != mStarFriendList.size + 1) {
//                    val friend = mStarFriendList.get(position - 1)
//                    val userType = friend.userType
//                    if (Constant.USER_TYPE_REG == userType) {
//                        if (friend.userId == mUser.getUserId()) {
//                            startActivity(Intent(activity, UserInfoMyActivity::class.java))
//                        } else {
//                            startActivity(
//                                Intent(activity, UserInfoActivity::class.java).putExtra
//                                    ("userId", friend.userId)
//                            )
//                        }
//                    } else if (Constant.USER_TYPE_WEIXIN == userType) {
//                        startActivity(
//                            Intent(activity, UserInfoActivity::class.java).putExtra
//                                ("userId", friend.userId)
//                        )
//                    } else if (Constant.USER_TYPE_FILEHELPER == userType) {
//                        startActivity(
//                            Intent(activity, UserInfoFileHelperActivity::class.java).putExtra
//                                ("userId", friend.userId)
//                        )
//                    }
//                }
//            }
//    }
//
//    fun refreshNewFriendsUnreadNum() {
//        val newFriendsUnreadNum = PreferencesUtil.getInstance().newFriendsUnreadNumber
//        if (newFriendsUnreadNum > 0) {
//            mNewFriendsUnreadNumTv!!.visibility = View.VISIBLE
//            mNewFriendsUnreadNumTv!!.text = newFriendsUnreadNum.toString()
//        } else {
//            mNewFriendsUnreadNumTv!!.visibility = View.GONE
//        }
//    }
//
//    fun refreshFriendsList() {
//        mStarFriendList = mUserDao!!.allStarredContactList
//        mFriendList = mUserDao!!.allFriendList
//
//        // 对list进行排序
//        Collections.sort(mFriendList, object : PinyinComparator() {
//        })
//        mStarFriendList.addAll(mFriendList)
//        mFriendsAdapter!!.setData(mStarFriendList)
//        mFriendsAdapter!!.notifyDataSetChanged()
//        mFriendsCountTv!!.text = mUserDao!!.contactsCount.toString() + "位联系人"
//    }
//}