//package com.example.endtoendencryptionsystem.ui
//
//import android.annotation.SuppressLint
//import android.app.Activity
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.content.pm.PackageManager
//import android.graphics.drawable.ColorDrawable
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.os.Handler
//import android.provider.Settings
//import android.text.TextUtils
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.PopupWindow
//import android.widget.RelativeLayout
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import androidx.localbroadcastmanager.content.LocalBroadcastManager
//import androidx.viewpager2.adapter.FragmentStateAdapter
//import androidx.viewpager2.widget.ViewPager2
//
//import com.bc.wechat.fragment.ChatsFragment
//import com.bc.wechat.fragment.ContactsFragment
//import com.bc.wechat.fragment.MeFragment
//import com.example.endtoendencryptionsystem.R
//import com.example.endtoendencryptionsystem.adapter.ViewPagerAdapter
//import com.example.endtoendencryptionsystem.core.BaseActivity
//import com.example.endtoendencryptionsystem.databinding.ActivityMainBinding
//import com.example.endtoendencryptionsystem.entiy.User
//import com.example.endtoendencryptionsystem.utils.json
//import com.example.endtoendencryptionsystem.utils.toObject
//import com.example.endtoendencryptionsystem.widget.ConfirmDialog
//import com.lnsoft.conslutationsystem.core.Config
//import com.lnsoft.conslutationsystem.core.SPConfig
//import com.tencent.mmkv.MMKV
//import com.therouter.TheRouter
//
///**
// * 主activity
// */
//class MainActivity : AppCompatActivity(){
//    private lateinit var binding: ActivityMainBinding
//    private lateinit var mChatsFragment: ChatsFragment
//    private lateinit var mContactsFragment: ContactsFragment
//    private lateinit var mMeFragment: MeFragment
//    private var fmList = ArrayList<Fragment>()
//    private lateinit var vpAdapter: FragmentStateAdapter
//
//    private lateinit var mMainButtonIvs: Array<ImageView?>
//    private lateinit var mMainButtonTvs: Array<TextView?>
//    private var mIndex = 0
//
//    // 当前fragment的index
//    private var mCurrentTabIndex = 0
//
//    var mUser: User? = null
//
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        TheRouter.inject(this)
//        initView()
//        initData()
//    }
//
//    fun initView() {
//
//    }
//
//    fun initData() {
//        val userJson = MMKV.defaultMMKV().decodeString(SPConfig.USER)
//        mUser = userJson?.let { json.toObject<User>(it) }
//        registerMessageReceiver()
//        refreshNewMsgsUnreadNum()
//        refreshNewFriendsUnreadNum()
//
//        mChatsFragment = ChatsFragment()
//        mContactsFragment = ContactsFragment()
//        mMeFragment = MeFragment()
//        fmList.add(mChatsFragment)
//        fmList.add(mContactsFragment)
//        fmList.add(mMeFragment)
//        // 进入强制刷新，防止离线消息
//        mChatsFragment.refreshConversationList()
//
//        vpAdapter = ViewPagerAdapter(this, fmList)
//        binding.vp.adapter = vpAdapter
//        binding.vp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                if (position == 0) {
//                    binding.ivChats.isSelected = true
//                    binding.ivContacts.isSelected = false
//                    binding.ivMe.isSelected = false
//                } else if(position == 1){
//                    binding.ivChats.isSelected = false
//                    binding.ivContacts.isSelected = true
//                    binding.ivMe.isSelected = false
//                } else {
//                    binding.ivChats.isSelected = false
//                    binding.ivContacts.isSelected = false
//                    binding.ivMe.isSelected = true
//                }
//            }
//        })
//    }
//
//    fun onTabClicked(view: View) {
//        when (view.id) {
//            R.id.rl_chats -> {
//                // 会话列表
//                // 主动加载一次会话
//                mChatsFragment.refreshConversationList()
//                binding.ivChats.isSelected = true
//                binding.ivContacts.isSelected = false
//                binding.ivMe.isSelected = false
//            }
//
//            R.id.rl_contacts -> {
//                binding.ivChats.isSelected = false
//                binding.ivContacts.isSelected = true
//                binding.ivMe.isSelected = false
//            }
//
//            R.id.rl_me -> {
//                binding.ivChats.isSelected = false
//                binding.ivContacts.isSelected = false
//                binding.ivMe.isSelected = true
//            }
//        }
//
//    }
//
//    override fun onResume() {
//        super.onResume()
//        isForeground = true
//        // 消息
//        refreshNewMsgsUnreadNum()
//        // 通讯录
//        refreshNewFriendsUnreadNum()
//        mContactsFragment!!.refreshNewFriendsUnreadNum()
//        mContactsFragment!!.refreshFriendsList()
//
//        // 会话
//        if (mCurrentTabIndex == 0) {
//            mChatsFragment!!.refreshConversationList()
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        isForeground = false
//    }
//
//    private var mMessageReceiver: MessageReceiver? = null
//    fun registerMessageReceiver() {
//        mMessageReceiver = MessageReceiver()
//        val filter = IntentFilter()
//        filter.priority = IntentFilter.SYSTEM_HIGH_PRIORITY
//        filter.addAction(MESSAGE_RECEIVED_ACTION_ADD_FRIENDS_APPLY_MAIN)
//        filter.addAction(MESSAGE_RECEIVED_ACTION_ADD_FRIENDS_ACCEPT_MAIN)
//        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver!!, filter)
//    }
//
////    override fun receiveMsg(viewType: Int, msg: Message) {
////        if (ViewType.MAIN.type == viewType) {
////            handleReceivedMessage(msg)
////        }
////    }
//
//    inner class MessageReceiver : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            try {
//                if (MESSAGE_RECEIVED_ACTION_ADD_FRIENDS_APPLY_MAIN == intent.action) {
//                    val message = intent.getStringExtra(KEY_MESSAGE)
//                    val extras = intent.getStringExtra(KEY_EXTRAS)
//                    val showMsg = StringBuilder()
//                    showMsg.append(KEY_MESSAGE + " : " + message + "\n")
//                    if (!TextUtils.isEmpty(extras)) {
//                        showMsg.append(KEY_EXTRAS + " : " + extras + "\n")
//                    }
//                    refreshNewFriendsUnreadNum()
//                    mContactsFragment!!.refreshNewFriendsUnreadNum()
//                }
//                if (MESSAGE_RECEIVED_ACTION_ADD_FRIENDS_ACCEPT_MAIN == intent.action) {
//                    mContactsFragment!!.refreshFriendsList()
//                }
//            } catch (e: Exception) {
//            }
//        }
//    }
//
//    fun refreshNewMsgsUnreadNum() {
//        val message = android.os.Message()
//        message.what = 1
//        handler.sendMessage(message)
//    }
//
//    private fun refreshNewFriendsUnreadNum() {
//        val newFriendsUnreadNum = MMKV.defaultMMKV().decodeInt(SPConfig.UN_READ_FRIEND_NUM)
//        if (newFriendsUnreadNum > 0) {
//            binding.unreadAddressNumber.visibility = View.VISIBLE
//            binding.unreadAddressNumber.text = newFriendsUnreadNum.toString()
//        } else {
//            binding.unreadAddressNumber.visibility = View.GONE
//        }
//    }
//
//
//    private val handler: Handler = @SuppressLint("HandlerLeak")
//    object : Handler() {
//        override fun handleMessage(msg: android.os.Message) {
//            if (msg.what == 1) {
//                val newMsgsUnreadNum = MMKV.defaultMMKV().decodeInt(SPConfig.UN_READ_MSG_NUM)
//                if (newMsgsUnreadNum > 0) {
//                    binding.unreadMsgNumber.visibility = View.VISIBLE
//                    binding.unreadMsgNumber.text = newMsgsUnreadNum.toString()
//                } else {
//                    binding.unreadMsgNumber.visibility = View.GONE
//                }
//            }
//        }
//    }
//
//
//    override fun checkSelfPermission(permission: String): Int {
//        return super.checkSelfPermission(permission)
//    }
//
//
//
//    /**
//     * 动态权限
//     */
//    fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
//        //Android 6.0开始的动态权限，这里进行版本判断
//        val mPermissionList = ArrayList<String>()
//        for (i in permissions.indices) {
//            if (ContextCompat.checkSelfPermission(activity, permissions[i])
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                mPermissionList.add(permissions[i])
//            }
//        }
//        if (mPermissionList.isEmpty()) {
//            // 非初次进入App且已授权
//            when (requestCode) {
//                REQUEST_CODE_CAMERA -> startScanActivity()
//            }
//        } else {
//            // 请求权限方法
//            val requestPermissions = mPermissionList.toTypedArray<String>()
//            // 这个触发下面onRequestPermissionsResult这个回调
//            ActivityCompat.requestPermissions(this, requestPermissions, requestCode)
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        var hasAllGranted = true
//        // 判断是否拒绝  拒绝后要怎么处理 以及取消再次提示的处理
//        for (grantResult in grantResults) {
//            if (grantResult == PackageManager.PERMISSION_DENIED) {
//                hasAllGranted = false
//                break
//            }
//        }
//        if (hasAllGranted) {
//            // 同意权限做的处理,开启服务提交通讯录
//            when (requestCode) {
//                REQUEST_CODE_CAMERA -> startScanActivity()
//            }
//        } else {
//            // 拒绝授权做的处理，弹出弹框提示用户授权
//            handleRejectPermission(this, permissions[0], requestCode)
//        }
//    }
//
//    fun handleRejectPermission(
//        context: Activity, permission: String,
//        requestCode: Int
//    ) {
//        if (!ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
//            var content = ""
//            // 非初次进入App且已授权
//            when (requestCode) {
//                REQUEST_CODE_CAMERA -> content = getString(R.string.request_permission_camera)
//            }
//            if (!TextUtils.isEmpty(content)) {
//                val mConfirmDialog = ConfirmDialog(
//                    this@MainActivity, "权限申请",
//                    content,
//                    "去设置", "取消", context.getColor(R.color.navy_blue)
//                )
//                mConfirmDialog.setOnDialogClickListener(object :
//                    ConfirmDialog.OnDialogClickListener {
//                    override fun onOkClick() {
//                        mConfirmDialog.dismiss()
//                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                        val uri =
//                            Uri.fromParts("package", context.applicationContext.packageName, null)
//                        intent.setData(uri)
//                        context.startActivity(intent)
//                    }
//
//                    override fun onCancelClick() {
//                        mConfirmDialog.dismiss()
//                    }
//                })
//                // 点击空白处消失
//                mConfirmDialog.setCancelable(false)
//                mConfirmDialog.show()
//            }
//        }
//    }
//
//    /**
//     * 进入扫一扫页面
//     */
//    private fun startScanActivity() {
////        Intent intent = new Intent(this, CaptureActivity2.class);
////        intent.putExtra(CaptureActivity2.USE_DEFUALT_ISBN_ACTIVITY, true);
////        startActivityForResult(intent, REQUEST_CODE_SCAN);
//    }
//
//    companion object {
//        const val REQUEST_CODE_SCAN: Int = 0
//        const val REQUEST_CODE_CAMERA: Int = 1
//        const val REQUEST_CODE_LOCATION: Int = 2
//
//        @JvmField
//        var isForeground: Boolean = false
//
//        const val MESSAGE_RECEIVED_ACTION_ADD_FRIENDS_APPLY_MAIN: String =
//            "com.bc.wechat.MESSAGE_RECEIVED_ACTION_ADD_FRIENDS_APPLY_MAIN"
//        const val MESSAGE_RECEIVED_ACTION_ADD_FRIENDS_APPLY_NEW_FRIENDS_MSG: String =
//            "com.bc.wechat.MESSAGE_RECEIVED_ACTION_ADD_FRIENDS_APPLY_NEW_FRIENDS_MSG"
//
//        const val MESSAGE_RECEIVED_ACTION_ADD_FRIENDS_ACCEPT_MAIN: String =
//            "com.bc.wechat.MESSAGE_RECEIVED_ACTION_ADD_FRIENDS_ACCEPT_MAIN"
//        const val KEY_MESSAGE: String = "message"
//        const val KEY_EXTRAS: String = "extras"
//    }
//}