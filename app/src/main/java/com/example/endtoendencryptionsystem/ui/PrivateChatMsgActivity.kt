package com.example.endtoendencryptionsystem.ui


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.drake.statusbar.immersive
import com.example.endtoendencryptionsystem.R
import com.example.endtoendencryptionsystem.adapter.EmojiAdapter
import com.example.endtoendencryptionsystem.adapter.EmojiPagerAdapter
import com.example.endtoendencryptionsystem.adapter.MessageAdapter
import com.example.endtoendencryptionsystem.adapter.PrivateMsgAdapter
import com.example.endtoendencryptionsystem.databinding.ActivityChatBinding
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.PrivateChatMessage
import com.example.endtoendencryptionsystem.entiy.database.PrivateMessage
import com.example.endtoendencryptionsystem.entiy.dto.PrivateMessageDTO
import com.example.endtoendencryptionsystem.enums.MessageType
import com.example.endtoendencryptionsystem.viewmodel.ChatViewModel
import com.example.endtoendencryptionsystem.viewmodel.FriendViewModel
import com.example.endtoendencryptionsystem.widget.ConfirmDialog
import com.example.endtoendencryptionsystem.widget.ConfirmDialog.OnDialogClickListener
import com.example.endtoendencryptionsystem.widget.ExpandGridView
import com.tencent.mmkv.MMKV
import java.io.File
import java.util.UUID


/**
 * 私聊页面
 * 主要逻辑点：
 * 1，在发送第一条消息时，获取下好友密钥（以防密钥更新）
 */
class PrivateChatMsgActivity : AppCompatActivity(){
    val REQUEST_CODE_VOICE: Int = 5
    val REQUEST_CODE_IMAGE_ALBUM: Int = 6
    val REQUEST_CODE_IMAGE_CAMERA: Int = 7
    val REQUEST_CODE_LOCATION: Int = 8
    private lateinit var binding: ActivityChatBinding
    private val viewModel by viewModels<ChatViewModel>()
    private val vmFriend by viewModels<FriendViewModel>()
    private lateinit var friendInfo: Friend
    private  var conversationId:Long = 0
    private lateinit var adapter: MessageAdapter
    private var data = ArrayList<PrivateChatMessage>()
    private lateinit var mManager: InputMethodManager
    lateinit var permissions: Array<String>
    lateinit var mEmojiList: MutableList<String>
    lateinit var mVoiceRecordingAd: AnimationDrawable

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        immersive()
        initView()
        getData()
        obsever()

    }

    //获取本地数据库消息
    fun getData(){
        conversationId  = intent.getLongExtra("conversationId", 0)
        viewModel.getPrivateMsgByConversationId(conversationId)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun obsever(){
        //发送消息后更新UI
        viewModel.sendPrivateMsgResult.observe(this){
            Log.e("发送消息后更新UI","--")
            binding.etTextMsg.text.clear()
        }

        //获取私聊消息后更新UI
        viewModel.getPrivateMsgByConversationId.observe(this) {
            Log.e("获取消息后更新UI","--")
            data.clear()
            data.addAll(it)
            adapter.notifyDataSetChanged()
        }

        //获取好友信息后再发送消息
        vmFriend.syncFriendInfo.observe(this) {
            var content = binding.etTextMsg.text.toString()
            val privateMessageDTO = PrivateMessageDTO(
                recvId = friendInfo.friendId.toLong(),
                messageId = UUID.randomUUID().toString(),
                content = content,
                type = MessageType.TEXT.code
            )
            viewModel.sendPrivateMessage(friendInfo.friendNickName.toString(),privateMessageDTO)
        }


    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("NotifyDataSetChanged", "ClickableViewAccessibility")
    fun initView() {
        friendInfo = intent.getParcelableExtra<Friend>("friendInfo")!!
        binding.layoutTitle.tvTitle.text = friendInfo.friendNickName
        binding.layoutTitle.ivSettings.visibility = View.VISIBLE
        binding.layoutTitle.ivAdd.visibility = View.GONE
        binding.layoutTitle.ivSearch.visibility = View.GONE
        binding.layoutTitle.ivBack.setOnClickListener { finish() }



        mManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )
        // 聊天页拉至最下
        binding.rv.scrollToPosition(data.size- 1)

        //用户信息
        binding.layoutTitle.ivSettings.setOnClickListener {
            val intent = Intent(this, FriendInfoActivity::class.java)
            intent.putExtra("friendInfo", friendInfo)
            startActivity(intent)
        }

        adapter = MessageAdapter(data,friendInfo)
        binding.rv.layoutManager = LinearLayoutManager(this)
        binding.rv.adapter = adapter
        adapter.addChildClickViewIds(R.id.iv_tou)

        adapter.setOnItemChildClickListener { _, view, position ->
            if(view.id == R.id.iv_tou){
                val intent = Intent(this, FriendInfoActivity::class.java)
                intent.putExtra("friendInfo", friendInfo)
                startActivity(intent)
            }
        }


        //展开更多发送选项
        binding.btnMore.setOnClickListener {
            more(it)
        }

        binding.etTextMsg.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(view: View?, hasFocus: Boolean) {
                if (hasFocus) {
                    // 获取焦点
                    // 隐藏消息类型容器
                    binding.llBtnContainer.visibility = View.GONE
                    // 聊天页拉至最下
                    binding.rv.scrollToPosition(data.size- 1)
                    // 隐藏表情
                    binding.ivEmojiChecked.visibility = View.GONE
                    binding.ivEmojiNormal.visibility = View.VISIBLE
                    binding.llEmojiContainer.visibility = View.GONE
                }
            }
        }


        binding.etTextMsg.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {}
            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                if (TextUtils.isEmpty(charSequence)) {
                    binding.btnMore.visibility = View.VISIBLE
                    binding.btnSend.visibility = View.GONE
                } else {
                    binding.btnMore.visibility = View.GONE
                    binding.btnSend.visibility = View.VISIBLE
                }
            }
            override fun afterTextChanged(editable: Editable?) {
            }
        })

        //发送消息
        binding.btnSend.setOnClickListener {
            sendTextMsg()
        }

        //切换成语音输入
        binding.btnSetModeVoice.setOnClickListener {
            // 切换成语音
            permissions = arrayOf<String>("android.permission.RECORD_AUDIO")
            requestPermissions(
                this@PrivateChatMsgActivity,
                permissions,
                REQUEST_CODE_VOICE
            )
        }

        //切换成文字
        binding.btnSetModeKeyboard.setOnClickListener {
            // 输入框获取焦点
            // 显示软键盘
            binding.etTextMsg.setFocusable(true)
            binding.etTextMsg.isFocusableInTouchMode = true
            binding.etTextMsg.requestFocus()
            showKeyboard()
            binding.llPressToSpeak.visibility = View.GONE
            binding.rlTextMsg.visibility = View.VISIBLE
            binding.btnSetModeKeyboard.visibility = View.GONE
            binding.btnSetModeVoice.visibility = View.VISIBLE
        }

        //相册
        binding.llImageAlbum.setOnClickListener {
            Toast.makeText(this, "相册功能待开发", Toast.LENGTH_SHORT).show()
        }

        //拍照
        binding.llImageCamera.setOnClickListener {
            Toast.makeText(this, "拍照功能待开发", Toast.LENGTH_SHORT).show()
        }

        binding.ivEmojiNormal.setOnClickListener {
            hideKeyboard()
            binding.llMore.visibility = View.VISIBLE
            binding.ivEmojiNormal.visibility = View.GONE
            binding.ivEmojiChecked.visibility = View.VISIBLE
            binding.llEmojiContainer.visibility = View.VISIBLE

            binding.llBtnContainer.visibility = View.GONE
            // 切换成文字
            binding.llPressToSpeak.visibility = View.GONE
            binding.rlTextMsg.visibility = View.VISIBLE
            binding.btnSetModeKeyboard.visibility = View.GONE
            binding.btnSetModeVoice.visibility = View.VISIBLE
        }

        binding.ivEmojiChecked.setOnClickListener {
            binding.ivEmojiNormal.visibility = View.VISIBLE
            binding.ivEmojiChecked.visibility = View.GONE
            binding.llEmojiContainer.visibility = View.GONE
            binding.llBtnContainer.visibility = View.GONE
        }

        binding.rv.setOnTouchListener(object : OnTouchListener {
            @SuppressLint("UseKtx")
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                hideKeyboard()
                // 隐藏更多
                if (binding.llEmojiContainer.isVisible) {
                    binding.llEmojiContainer.visibility = View.GONE
                    binding.llBtnContainer.visibility = View.VISIBLE
                    binding.ivEmojiNormal.visibility = View.VISIBLE
                    binding.ivEmojiChecked.visibility = View.GONE
                } else {
                    binding.llMore.visibility = View.GONE
                }
                return false
            }
        })


        // 表情
        initEmojiList(40)
        val emojiViews: MutableList<View?> = java.util.ArrayList<View?>()
        val emojiView1 = getGridChildView(1)
        val emojiView2 = getGridChildView(2)
        emojiViews.add(emojiView1)
        emojiViews.add(emojiView2)
        binding.vpEmoji.setAdapter(EmojiPagerAdapter(emojiViews))


        //按住说话listener
        binding.llPressToSpeak.setOnTouchListener { view, motionEvent ->
            when (motionEvent.getAction()) {
                MotionEvent.ACTION_DOWN -> {
                    try {
                        view.setPressed(true)
                        // 播放动画
                        mVoiceRecordingAd = binding.ivVoiceRecordingAnim.drawable as AnimationDrawable
                        mVoiceRecordingAd.start()

                        binding.rlVoiceRecordingContainer.visibility = View.VISIBLE
                        binding.tvVoiceRecordingHint.text = getString(R.string.move_up_to_cancel)
                        binding.tvVoiceRecordingHint.setBackgroundColor(Color.TRANSPARENT)
                    } catch (e: Exception) {
                        view.isPressed = false
                        binding.rlVoiceRecordingContainer.visibility = View.INVISIBLE
                        false
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (motionEvent.getY() < 0) {
                        binding.tvVoiceRecordingHint.text = getString(R.string.release_to_cancel)
                        binding.tvVoiceRecordingHint.setBackgroundResource(R.drawable.recording_text_hint_bg)
                    } else {
                        binding.tvVoiceRecordingHint.text = getString(R.string.move_up_to_cancel)
                        binding.tvVoiceRecordingHint.setBackgroundColor(Color.TRANSPARENT)
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    view.isPressed = false
                    binding.rlVoiceRecordingContainer.visibility = View.INVISIBLE
                    true
                }

                else -> {
                    binding.rlVoiceRecordingContainer.visibility = View.INVISIBLE
                    false
                }
            }
        }

        initCamera()


    }



    /**
     * 发送消息
     */
    fun sendTextMsg(){
        var content = binding.etTextMsg.text.toString()
        if (TextUtils.isEmpty(content)) {
            return
        }
        val privateMessageDTO = PrivateMessageDTO(
            recvId = friendInfo.friendId.toLong(),
            messageId = UUID.randomUUID().toString(),
            content = content,
            type = MessageType.TEXT.code
        )
        val userId = MMKV.defaultMMKV().decodeInt("userId")
        if(data.isEmpty()){
            vmFriend.getFriendById(friendInfo.friendId)
        }else{
            viewModel.sendPrivateMessage(friendInfo.friendNickName.toString(),privateMessageDTO)
        }

    }


    /**
     * 显示或隐藏图标按钮页
     *
     * @param view
     */
    @SuppressLint("UseKtx")
    fun more(view: View?) {
        if (binding.llMore.isGone) {
            hideKeyboard()
            binding.llMore.visibility = View.VISIBLE
            binding.llBtnContainer.visibility = View.VISIBLE
            // 切换成文字
            binding.llPressToSpeak.visibility = View.GONE
            binding.rlTextMsg.visibility = View.VISIBLE
            binding.btnSetModeKeyboard.visibility = View.GONE
            binding.btnSetModeVoice.visibility = View.VISIBLE

        } else {
            if (binding.llEmojiContainer.visibility == View.VISIBLE) {
                binding.llEmojiContainer.visibility = View.GONE
                binding.llBtnContainer.visibility = View.VISIBLE
                binding.ivEmojiNormal.visibility = View.VISIBLE
                binding.ivEmojiChecked.visibility = View.GONE
            } else {
                binding.llMore.visibility = View.GONE
            }
        }
    }

    /**
     * 点击文字输入框
     *
     * @param v
     */
    fun editClick(v: View?) {
        binding.rv.scrollToPosition(data.size- 1)
        if (binding.llMore.isVisible) {
            binding.llMore.visibility = View.GONE
            binding.ivEmojiNormal.visibility = View.VISIBLE
            binding.ivEmojiChecked.visibility = View.GONE
        }
    }

    /**
     * 隐藏软键盘
     */
    private fun hideKeyboard() {
        if (getWindow().getAttributes().softInputMode !== WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null) mManager.hideSoftInputFromWindow(
                getCurrentFocus()!!
                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    /**
     * 隐藏软键盘
     */
    private fun showKeyboard() {
        if (getWindow().getAttributes().softInputMode !== WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE) {
            if (getCurrentFocus() != null) {
                mManager.showSoftInput(binding.etTextMsg, 0)
            }
        }
    }


    /**
     * 动态权限
     */
    fun requestPermissions(activity: AppCompatActivity, permissions: Array<String>, requestCode: Int) {
        // Android 6.0开始的动态权限，这里进行版本判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val mPermissionList = java.util.ArrayList<String?>()
            for (i in permissions.indices) {
                if (ContextCompat.checkSelfPermission(activity, permissions[i])
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    mPermissionList.add(permissions[i])
                }
            }
            if (mPermissionList.isEmpty()) {
                // 非初次进入App且已授权
                when (requestCode) {
                    REQUEST_CODE_LOCATION -> showMapPicker()
                    REQUEST_CODE_IMAGE_ALBUM -> showAlbum()
                    REQUEST_CODE_IMAGE_CAMERA -> showCamera()
                    REQUEST_CODE_VOICE -> showAudio()
                }
            } else {
                // 请求权限方法
                val requestPermissions = mPermissionList.toTypedArray<String?>()
                // 这个触发下面onRequestPermissionsResult这个回调
                ActivityCompat.requestPermissions(activity, requestPermissions, requestCode)
            }
        }
    }

    /**
     * requestPermissions的回调
     * 一个或多个权限请求结果回调
     */
    public override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var hasAllGranted = true
        // 判断是否拒绝  拒绝后要怎么处理 以及取消再次提示的处理
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                hasAllGranted = false
                break
            }
        }
        if (hasAllGranted) {
            when (requestCode) {
                REQUEST_CODE_LOCATION -> showMapPicker()
                REQUEST_CODE_IMAGE_ALBUM -> showAlbum()
                REQUEST_CODE_IMAGE_CAMERA -> showCamera()
                REQUEST_CODE_VOICE -> showAudio()
            }
        } else {
            // 拒绝授权做的处理，弹出弹框提示用户授权
            handleRejectPermission(this@PrivateChatMsgActivity, permissions[0]!!, requestCode)
        }
    }

    fun handleRejectPermission(context: Activity, permission: String, requestCode: Int) {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
            var content: String? = ""
            // 非初次进入App且已授权
            when (requestCode) {
                REQUEST_CODE_LOCATION -> content =
                    getString(R.string.request_permission_location)
                REQUEST_CODE_IMAGE_ALBUM -> content =
                    getString(R.string.request_permission_storage)
                REQUEST_CODE_IMAGE_CAMERA -> content =
                    getString(R.string.request_permission_camera)
                REQUEST_CODE_VOICE -> content =
                    getString(R.string.request_permission_record_audio)
            }

            val mConfirmDialog: ConfirmDialog = ConfirmDialog(
                this@PrivateChatMsgActivity,
                getString(R.string.request_permission),
                content,
                getString(R.string.go_setting),
                getString(R.string.cancel),
                getColor(R.color.navy_blue)
            )
            mConfirmDialog.setOnDialogClickListener(object : OnDialogClickListener {
                public override fun onOkClick() {
                    mConfirmDialog.dismiss()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts(
                        "package",
                        context.getApplicationContext().getPackageName(),
                        null
                    )
                    intent.setData(uri)
                    context.startActivity(intent)
                }

                public override fun onCancelClick() {
                    mConfirmDialog.dismiss()
                }
            })
            // 点击空白处消失
            mConfirmDialog.setCancelable(false)
            mConfirmDialog.show()
        }
    }


    /**
     * 进入地图选择页面 TODO 待定
     */
    private fun showMapPicker() {
//        val intent: Intent = Intent(this@PrivateChatMsgActivity, MapPickerActivity::class.java)
//        intent.putExtra("sendLocation", true)
//        intent.putExtra("locationType", Constant.LOCATION_TYPE_MSG)
//        startActivityForResult(intent, com.bc.wechat.activity.ChatActivity.REQUEST_CODE_LOCATION)
    }

    /**
     * 跳转到相机
     */
    private fun showCamera() {
        var mImageName = UUID.randomUUID().toString().replace("-".toRegex(), "") + ".png"
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(
            MediaStore.EXTRA_OUTPUT, Uri.fromFile(
                File(Environment.getExternalStorageDirectory(), mImageName)
            )
        )
        startActivityForResult(cameraIntent, REQUEST_CODE_IMAGE_CAMERA)
    }

    /**
     * 跳转到相册
     */
    private fun showAlbum() {
        val intent = Intent(Intent.ACTION_PICK, null)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intent, REQUEST_CODE_IMAGE_ALBUM)
    }

    /**
     * 进入录音模式
     */
    private fun showAudio() {
        // 切换成语音
        hideKeyboard()
        // 隐藏消息类型容器
        binding.llBtnContainer.visibility = View.GONE
        // 隐藏表情
        binding.ivEmojiChecked.visibility = View.GONE
        binding.ivEmojiNormal.visibility = View.VISIBLE
        binding.llEmojiContainer.visibility = View.GONE

        // 显示"按住说话"
        binding.llPressToSpeak.visibility = View.VISIBLE
        // 隐藏文本输入框
        binding.rlTextMsg.visibility = View.GONE

        binding.btnSetModeVoice.visibility = View.GONE
        binding.btnSetModeKeyboard.visibility = View.VISIBLE
    }

    /**
     * android 7.0系统解决拍照的问题
     */
    private fun initCamera() {
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()
    }

    /**
     * 初始化emoji列表
     *
     * @param emojiNum emoji数量
     */
    private fun initEmojiList(emojiNum: Int) {
        mEmojiList = ArrayList<String>()
        for (i in 1..emojiNum) {
            if (i < 10) {
                mEmojiList.add("emoji_0" + i)
            } else {
                mEmojiList.add("emoji_" + i)
            }
        }
    }

    private fun getGridChildView(i: Int): View {
        val view = View.inflate(this, R.layout.gridview_emoji, null)
        val expandGridView: ExpandGridView = view.findViewById(R.id.egv_emoji)
        val emojiList: MutableList<String?> = java.util.ArrayList<String?>()
        if (i == 1) {
            emojiList.addAll(mEmojiList.subList(0, 21))
        } else {
            emojiList.addAll(mEmojiList.subList(21, mEmojiList.size))
        }
        emojiList.addAll(mEmojiList)
        emojiList.add("delete_emoji")
        val emojiAdapter: EmojiAdapter = EmojiAdapter(this, 1, emojiList)
        expandGridView.setAdapter(emojiAdapter)

        return view
    }

//    var handler: Handler = @SuppressLint("HandlerLeak")
//    object : Handler() {
//        override fun handleMessage(msg: Message) {
//            super.handleMessage(msg)
//            when (msg.what) {
//                REQUEST_CODE_IMAGE_ALBUM -> {
//                    val imgUrl = msg.getData().getString("imgUrl")
//                    val messageId = msg.getData().getString("messageId")
//                    val localPath = msg.getData().getString("localPath")
//                    sendImageMsg(imgUrl, messageId, localPath)
//                }
//            }
//        }
//    }


}

