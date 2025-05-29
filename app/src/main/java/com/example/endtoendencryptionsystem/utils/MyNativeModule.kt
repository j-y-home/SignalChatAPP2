package com.example.endtoendencryptionsystem.utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.example.endtoendencryptionsystem.ETEApplication.Companion.getInstance
import com.example.endtoendencryptionsystem.entiy.PersistentSignalProtocolStore
import com.example.endtoendencryptionsystem.entiy.database.ChatConversation
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.Group
import com.example.endtoendencryptionsystem.entiy.database.User
import com.example.endtoendencryptionsystem.model.PreKeyBundleMaker
import com.example.endtoendencryptionsystem.repository.ChatRepository
import com.example.endtoendencryptionsystem.repository.KeyRepository
import com.example.endtoendencryptionsystem.rsa.group.GroupCipherUtil
import com.example.endtoendencryptionsystem.rsa.group.GroupSessionUtil
import com.tencent.mmkv.MMKV
import io.dcloud.feature.uniapp.annotation.UniJSMethod
import io.dcloud.feature.uniapp.bridge.UniJSCallback
import io.dcloud.feature.uniapp.common.UniModule
import org.whispersystems.libsignal.IdentityKey
import org.whispersystems.libsignal.SessionBuilder
import org.whispersystems.libsignal.SessionCipher
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.groups.state.SenderKeyStore
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.protocol.SenderKeyDistributionMessage
import org.whispersystems.libsignal.protocol.SignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.Random
import java.util.concurrent.Executor
import java.util.concurrent.Executors


/**
 * 在uniapp中改代码改的好费劲。
 * 就比如调用Android原生加解密方法的传参这里，我要在uniapp中获取到那些参数（好有的公钥等信息），就比较费劲，要改很多地方。
 * 我刚刚突然来了个灵感，但我不知道可不可行。
 * 就是uniapp既然人家是现成的做好的，我就尽量不在它里面改动。
 * 具体的加解密等所有相关的，我都放在Android数据库里，甚至必要时可以直接Android去调用接口。
 * 比如，用户加好友后，会有个好友列表，我把这个表数据存Android数据库。
 * 这样在Uniapp中点击某个好友聊天时，只需要传入该好友的id，来到Android这边找好友对应的信息（公钥等）去加密。
 * 问题一：好友的公钥等会定期更新吗，更新后，本地数据库要更新的问题。
 * 问题二：本地apk卸载后，好友信息也没了，可以做一键从服务器获取好友信息(其实同问题一的操作一样)。
 *
 * 甚至我现在做好的登录时调用方法获取密钥，并调用接口更新到user，
 * 都可以把当前用户的信息存到Sharepreferce中（目前是存到uni的setStorage里面的）
 * 登录这块，后期再改，目前虽然实现了，但是我觉得有bug。
 *
 * 最主要的是消息表，肯定是要存Android数据库的，因为服务器存的是加密的，存了也没用。
 *
 *
 * TODO 5.15:加好友、发消息、存消息、获取消息、删除会话及其关联消息、更新会话、更新消息已读状态、删除消息（有bug）已走通。
 * TODO 待处理bug0：加好友有bug，A加B，如果B不在线，那么就收不到websocket的消息，就无法在B的设备上存储好友信息。
 * 解决：从接口获取到好友信息后，调用下addFriend方法？
 * TODO 好友关系解除后，本地数据库表删除
 * TODO 待处理bug1 ：删除了会话，websocket又发送了消息，导致数据库中又插入了已删除的数据。
 * TODO 已处理bug2 ：删除消息，数据库中删除了，但是UI页面未删除。 已解决
 * TODO 待处理问题1：除了主动发送的消息，其他消息如何添加messageId（比如：”你们已经成为好友啦“）
 * TODO 待处理问题2：图片，文件在数据库中如何存储。
 * TODO 待处理问题3：加密部分
 * TODO 待处理问题4：群聊
 * TODO 待处理问题5：撤回消息
 * TODO 待处理问题5：置顶消息，杀掉进程，再启动，置顶失效。（处理数据库表里面会话的置顶状态）
 *
 * TODO 最新想法：
 * 简单设计：本地不存好友表，发送信息时把好友信息传过来。这样本地不用处理好友表的增删改查。
 * 没网时，只能显示以前的会话记录（本地表中的），无法发送消息。
 *
 */
class MyNativeModule : UniModule() {
    // 注册方法供 UniApp 调用（同步方法）
    private val chatRepository = ChatRepository(getInstance()!!)
    private val keyRepository = KeyRepository(getInstance()!!)
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val TAG: String = "MyNativeModule"
    private var senderKeyStore: SenderKeyStore? = null
    private lateinit var cipherUtil: GroupCipherUtil
    private lateinit var sessionUtil: GroupSessionUtil
    private lateinit var selfAddress: SignalProtocolAddress

//    @SuppressLint("NotConstructor")
//    fun MyNativeModule(context: Context?) {
    //    this.senderKeyStore = MySenderKeyStore()
//        this.cipherUtil = GroupCipherUtil(senderKeyStore)
//        this.sessionUtil = GroupSessionUtil(senderKeyStore)
//    }

    /**
     * 存储当前登录的用户信息到sp
     */
    @UniJSMethod(uiThread = false)
    fun storeUserInfoToSp(userInfo : String) {
        Log.e(TAG, "当前登录用户信息:"+userInfo)
        val currentUser:User = json.toObject(userInfo)
        MMKV.defaultMMKV().encode("currentUserId",currentUser.id)
        MMKV.defaultMMKV().encode("currentUser",currentUser)
    }


    /**
     * 改进后的注册密钥方法（登录后注册，现在是手动点击我的页面的注册按钮）
     * 返回的数据结构：
     * {"identityKey":"BasPxq4TWBFD3tzMg2uou81fs5jY3Re2U+9Z73gB4jMF","registrationId":7736,"signedPreKeys":[{"publicKey":"BWCEIBQ9CADQYlLYPPHy9sc4qXu1FPLfol5+fe7S8iN7","keyId":3,"timestamp":1747905064570,"signature":"1jFq1HIbUDrv6w1lWT2mCJj2nW7WkCK4ibsnqMJM+46YOrdwrl/Zg99QykQc+lnk3393eGLhkQlJ3VlNw3T2DA=="}],"preKeys":[{"publicKey":"BSrHexwl3KpkdoOhDnJGORCQx1W2ZGddm+/Iru32v7oV","keyId":69}]}
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @UniJSMethod(uiThread = false)
    fun registerKey(address: String, callback: UniJSCallback) {
        try {
            val currentUserId = MMKV.defaultMMKV().decodeLong("currentUserId").toString()
            // 1. 创建持久化的 SignalProtocolStore
            val store = PersistentSignalProtocolStore(keyRepository, currentUserId)

            // 2. 生成预密钥和签名预密钥
            val random = Random()
            val preKeyId = random.nextInt(100)
            val signedPreKeyId = random.nextInt(100)

            val preKeyPair = Curve.generateKeyPair()
            val signedPreKeyPair = Curve.generateKeyPair()
            val timestamp = System.currentTimeMillis()

            // 3. 签名签名预密钥
            val signedPreKeySignature = Curve.calculateSignature(
                store.identityKeyPair.privateKey,
                signedPreKeyPair.publicKey.serialize()
            )

            // 4. 存储密钥到数据库
            val preKeyRecord = PreKeyRecord(preKeyId, preKeyPair)
            val signedPreKeyRecord = SignedPreKeyRecord(
                signedPreKeyId, timestamp, signedPreKeyPair, signedPreKeySignature
            )

            store.storePreKey(preKeyId, preKeyRecord)
            store.storeSignedPreKey(signedPreKeyId, signedPreKeyRecord)

            // 5. 构建返回给前端的数据（包含预密钥信息）
            val data = JSONObject()

            // 基本身份信息
            data.put("registrationId", store.localRegistrationId)
            data.put("identityKey", Base64.getEncoder().encodeToString(
                store.identityKeyPair.publicKey.publicKey.serialize()
            ))

            // 预密钥信息
            val preKeysArray = JSONArray()
            val preKeyInfo = JSONObject()
            preKeyInfo.put("keyId", preKeyId)
            preKeyInfo.put("publicKey", Base64.getEncoder().encodeToString(
                preKeyPair.publicKey.serialize()
            ))
            preKeysArray.add(preKeyInfo)
            data.put("preKeys", preKeysArray)

            // 签名预密钥信息
            val signedPreKeysArray = JSONArray()
            val signedPreKeyInfo = JSONObject()
            signedPreKeyInfo.put("keyId", signedPreKeyId)
            signedPreKeyInfo.put("publicKey", Base64.getEncoder().encodeToString(
                signedPreKeyPair.publicKey.serialize()
            ))
            signedPreKeyInfo.put("signature", Base64.getEncoder().encodeToString(
                signedPreKeySignature
            ))
            signedPreKeyInfo.put("timestamp", timestamp)
            signedPreKeysArray.add(signedPreKeyInfo)
            data.put("signedPreKeys", signedPreKeysArray)
            callback.invoke(data)
        } catch (e: Exception) {
            Log.e("SignalError", "Key generation failed", e)
            callback.invoke(null)
        }
    }


    /**
     * 改进后的添加好友方法
     * 有个思路：不如一加入好友就初始化会话，好友表插入一条好友数据后就调用初始化会话方法，在会话表插入与该好友的会话信息。
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @UniJSMethod(uiThread = false)
    fun addFriend(friendJson: String?, callback: UniJSCallback) {
        try {
            val user = json.toObject<User>(friendJson.toString())
            val currentUserId = MMKV.defaultMMKV().decodeLong("currentUserId")
            // 1. 保存好友基本信息
            val friend = Friend(
                userId = currentUserId,
                friendId = user.id.toLong(),
                friendNickName = user.nickName,
                friendHeadImage = user.headImage,
                preKeyBundleMaker = user.preKeyBundleMaker?:""
            )
            chatRepository.addFriend(friend)
            //加入好友立刻初始化Session会话
            initPrivateSession(friend.friendId.toString(), friend.preKeyBundleMaker!!)
            callback.invoke(true)
        } catch (e: Exception) {
            callback.invoke(false)
        }
    }

    /**
     * 根据好友Id获取好友信息
     * @param friendId
     * @return
     */
    fun getFriendInfoById(friendId: Long): Friend {
        val list: MutableList<Friend> = chatRepository.selectAllData() as MutableList<Friend>
        Log.e(TAG, list.size.toString() + "数据库好友表全部数据：" + JSONObject.toJSONString(list))
        var friend: Friend? = null
        friend = chatRepository.selectFriendsByFriendId(friendId)
        if(friend == null){
            Log.e(TAG, "当前对话的好友信息查不到" )
        }
        Log.e(TAG, "当前对话的好友信息：" + json.toJSONString(friend))
        return friend
    }


    /**
     * 改进后的建立私聊初始化会话方法（加入好友立马建立初始化session会话，添加会话信息到数据库表）
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun initPrivateSession(friendId: String, preKeyBundleJson: String) {
        try {
            val currentUserId = MMKV.defaultMMKV().decodeLong("currentUserId").toString()

            // 1. 创建持久化的 SignalProtocolStore
            val store = PersistentSignalProtocolStore(keyRepository, currentUserId)
            // 2. 解析好友的密钥信息
            val preKeyBundleMaker = json.toObject<PreKeyBundleMaker>(preKeyBundleJson)
            // 3. 创建好友的 SignalProtocolAddress
            val friendAddress = SignalProtocolAddress(friendId, 1)
            // 4. 检查是否已存在会话
            if (store.containsSession(friendAddress)) {
                Log.d("SessionInit", "Session already exists for friend: $friendId")
                return
            }
            // 5. 从新格式中获取密钥信息构建PreKeyBundle
            val identityKeyBytes = Base64.getDecoder().decode(preKeyBundleMaker.identityKey)
            val ecPublicKey = Curve.decodePoint(identityKeyBytes, 0)
            val identityKey = IdentityKey(ecPublicKey)

            // 选择第一个可用的预密钥和签名预密钥
            val preKey = preKeyBundleMaker.preKeys?.firstOrNull()
            val signedPreKey = preKeyBundleMaker.signedPreKeys?.firstOrNull()

            if (preKey == null || signedPreKey == null) {
                Log.e("SessionInit", "Missing required keys for friend: $friendId")
                return
            }
            // 6. 构建PreKeyBundle
            var ecPreKeyPublicKey = Curve.decodePoint(Base64.getDecoder().decode(preKey.publicKey), 0)
            var ecSignedPreKeyPublicKey = Curve.decodePoint(Base64.getDecoder().decode(signedPreKey.publicKey), 0)
            val preKeyBundle = PreKeyBundle(
                preKeyBundleMaker.registrationId,
                1, // deviceId
                preKey.keyId,
                ecPreKeyPublicKey,
                signedPreKey.keyId,
                ecSignedPreKeyPublicKey,
                Base64.getDecoder().decode(signedPreKey.signature),
                identityKey
            )
            // 7. 建立新会话
            val sessionBuilder = SessionBuilder(store, friendAddress)
            sessionBuilder.process(preKeyBundle)
            Log.d("SessionInit", "Session initialized successfully for friend: $friendId")
        } catch (e: Exception) {
            Log.e("SessionInit", "Failed to initialize session for friend: $friendId", e)
        }
    }

    /**
     * 私聊消息加密
     * @param friendId 好友ID
     * @param message 待加密的明文消息
     * @param callback 回调函数
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @UniJSMethod(uiThread = false)
    fun encryptPrivateMessage(friendId: String, message: String, callback: UniJSCallback) {
        try {
            val currentUserId = MMKV.defaultMMKV().decodeLong("currentUserId").toString()
            // 1. 创建持久化的 SignalProtocolStore
            val store = PersistentSignalProtocolStore(keyRepository, currentUserId)
            // 2. 创建好友的 SignalProtocolAddress
            val friendAddress = SignalProtocolAddress(friendId, 1)
            // 3. 检查会话是否存在
            if (!store.containsSession(friendAddress)) {
                Log.e("Encrypt", "No session exists for friend: $friendId")
                callback.invoke(null)
                return
            }
            // 4. 创建SessionCipher进行加密
            val sessionCipher = SessionCipher(store, friendAddress)
            val ciphertext = sessionCipher.encrypt(message.toByteArray(StandardCharsets.UTF_8));
            // 5. 将加密结果编码为Base64
            val encryptedBase64 = Base64.getEncoder().encodeToString( ciphertext.serialize())
            callback.invoke(encryptedBase64)

        } catch (e: Exception) {
            Log.e("Encrypt", "Failed to encrypt message for friend: $friendId", e)
            callback.invoke(null)
        }
    }

    /**
     * 私聊消息解密
     * @param friendId 好友ID
     * @param encryptedMessage Base64编码的加密消息
     * @param callback 回调函数
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @UniJSMethod(uiThread = false)
    fun decryptPrivateMessage(friendId: String, encryptedMessage: String, callback: UniJSCallback) {
        try {
            val currentUserId = MMKV.defaultMMKV().decodeLong("currentUserId").toString()
            // 1. 创建持久化的 SignalProtocolStore
            val store = PersistentSignalProtocolStore(keyRepository, currentUserId)
            // 2. 创建好友的 SignalProtocolAddress
            val friendAddress = SignalProtocolAddress(friendId, 1)
            // 3. 检查会话是否存在
            if (!store.containsSession(friendAddress)) {
                Log.e("Decrypt", "No session exists for friend: $friendId")
                callback.invoke("会话不存在")
                return
            }
            // 4. 解码Base64消息
            val encryptedBytes = Base64.getDecoder().decode(encryptedMessage)
            // 5. 创建SessionCipher进行解密
            val sessionCipher = SessionCipher(store, friendAddress)
            // 6. 尝试解密（可能是PreKeySignalMessage或SignalMessage）
            val decryptedBytes = try {
                // 首先尝试作为PreKeySignalMessage解密
                val preKeyMessage = PreKeySignalMessage(encryptedBytes)
                sessionCipher.decrypt(preKeyMessage)
            } catch (e: Exception) {
                // 如果失败，尝试作为SignalMessage解密
                val signalMessage = SignalMessage(encryptedBytes)
                sessionCipher.decrypt(signalMessage)
            }
            // 7. 转换为字符串
            val decryptedMessage = String(decryptedBytes, StandardCharsets.UTF_8)
            Log.d("Decrypt", "Message decrypted successfully for friend: $friendId")
            callback.invoke(decryptedMessage)
        } catch (e: Exception) {
            Log.e("Decrypt", "Failed to decrypt message for friend: $friendId", e)
            callback.invoke("解密失败")
        }
    }

    /**
     * 最新的设计：只保存最新一条收到的消息
     */
    @UniJSMethod(uiThread = false)
    fun saveNewMessage(messageInfo:String,chatInfo:String,userId:String){
        executor.execute(Runnable {
            try {
                val userIdLong = userId.toLong()
                chatRepository.saveNewMessage(messageInfo,chatInfo,userIdLong)
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Error in saveNewMessage", e)
            }
        })
    }

    /**
     * 更新会话
     */
    @UniJSMethod(uiThread = false)
    fun updateChat(chatInfo: ChatConversation){
        executor.execute(Runnable {
            try {
                chatRepository.updateChat(chatInfo)
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Error in updateChat", e)
            }
        })
    }

    /**
     * 更新消息已读状态
     */
    @UniJSMethod(uiThread = false)
    fun updateMessageReadStatus(messageIds: ArrayList<String>){
        executor.execute(Runnable {
            try {
                chatRepository.updateMessageReadStatus(messageIds)
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Error in updateChat", e)
            }
        })
    }

    /**
     * 删除会话及其关联的消息
     */
    @UniJSMethod(uiThread = false)
    fun deleteChat(chatId: Long,chatType: String){
        executor.execute(Runnable {
            try {
                chatRepository.deleteChat(chatId,chatType)
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Error in updateChat", e)
            }
        })
    }

    /**
     * 删除消息：根据messageId
     */
    @UniJSMethod(uiThread = false)
    fun deleteMessageByMessageId(messageId: String,type:String) {
        executor.execute(Runnable {
            try {
                val success = chatRepository.deleteMessageByMessageId(messageId,type)
                Log.e(TAG, "删除"+type+"消息："+success)
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "删除"+type+"消息：失败 "+e)
            }
        })
    }



    @UniJSMethod(uiThread = false)
    fun saveChats(userId: String, chatsToSave: JSONArray) {
        Log.e("xxx","调用saveChats方法：" + json.toJSONString(chatsToSave))
        executor.execute(Runnable {
            try {
                val userIdLong = userId.toLong()
                chatRepository.saveChats(userIdLong,chatsToSave)
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Error in saveChats", e)
            }
        })
    }

    @UniJSMethod(uiThread = false)
    fun getAllChats(userId: String, callback: UniJSCallback) {
        executor.execute({
            try {
                val userIdLong = userId.toLong()
                val result = chatRepository.getAllChats(userIdLong)
                Log.e(TAG,"获取到本地数据库消息："+json.toJSONString(result))
                callback.invoke(result)
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Error in getAllChats", e)
                callback.invoke(JSONObject())
            }
        })
    }

    @UniJSMethod(uiThread = false)
    fun deleteChats(userId: String, chatsToDelete: JSONArray, callback: UniJSCallback) {
        executor.execute(Runnable {
            try {
                val userIdLong = userId.toLong()
                val success = chatRepository.deleteChats(userIdLong, chatsToDelete)
                callback.invoke(success)
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Error in deleteChats", e)
                callback.invoke(false)
            }
        })
    }

    @UniJSMethod(uiThread = false)
    fun saveMetadata(userId: String, metadata: JSONObject) {
        executor.execute(Runnable {
            try {
                val userIdLong = userId.toLong()
                val success = chatRepository.saveMetadata(userIdLong, metadata)
                Log.e(TAG, "saveMetadata success: $success")
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Error in saveMetadata", e)
            }
        })
    }

    @UniJSMethod(uiThread = false)
    fun hasData(userId: String): Boolean {
        try {
            val userIdLong = userId.toLong()
            return chatRepository.hasData(userIdLong)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Error checking if data exists", e)
            return false
        }
    }

    /**
     * 添加群
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @UniJSMethod(uiThread = false)
    fun addGroup(groupJson: String, callback: UniJSCallback) {
        Log.e(TAG, "接收到的群信息：" + groupJson)
        try {
            var group = json.toObject<Group>(groupJson)
            chatRepository.addGroup(group)
        } catch (ignored: Exception) {
            Log.e(TAG, "添加群报错信息：" + ignored.message)
        }
    }

    /**
     * 新的数据覆盖
     */
    @UniJSMethod(uiThread = false)
    fun addGroups(groupJson: String, callback: UniJSCallback) {
        Log.e(TAG, "接收到的群信息：" + groupJson)
        try {
            chatRepository.clearGroup()
            var groups = json.toObject<List<Group>>(groupJson)
            chatRepository.addGroups(groups)
            callback.invoke(true)
        } catch (ignored: Exception) {
            Log.e(TAG, "批量添加更新群报错信息：" + ignored.message)
            callback.invoke(false)
        }
    }

    /**
     * 获取群组信息
     */
    @UniJSMethod(uiThread = false)
    fun getAllGroups(userId: String, callback: UniJSCallback) {
        executor.execute({
            try {
                val userIdLong = userId.toLong()
                val result = chatRepository.getAllGroups()
                Log.e(TAG,"获取到本地数据库群组信息："+json.toJSONString(result))
                callback.invoke(result)
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "Error in getAllGroups", e)
                callback.invoke(JSONObject())
            }
        })
    }

    /**
     * 更新群聊
     */
    @UniJSMethod(uiThread = false)
    fun updateGroup(groupJson: String){
        Log.e(TAG, "接收到的群组信息：" + groupJson)
        try {
            var group = json.toObject<Group>(groupJson)
            chatRepository.updateGroup(group)
        } catch (ignored: Exception) {
            Log.e(TAG, "更新群组报错信息：" + ignored.message)
        }
    }

    /**
     * 删除群聊
     */
    @UniJSMethod(uiThread = false)
    fun deleteGroup(groupId: Long, callback: UniJSCallback) {
        try {
            chatRepository.deleteGroup(groupId)
            callback.invoke(true)
        } catch (ignored: Exception) {
            Log.e(TAG, "删除群聊报错信息：" + ignored.message)
            callback.invoke(false)
        }
    }

    /***
     * ------------------------群聊的加密解密功能-----------------
     */

    /**
     * 正常的群聊加密机制太过复杂：
     * 1，多重会话密钥：每个群成员之间都需要建立独立的Signal会话，所以N个成员的群聊需要维护N×(N-1)/2个会话密钥对
     * 2，发送者密钥：每个发送者为群聊维护一个发送者密钥，用于加密发给所有成员的消息
     * 3，密钥轮换：当成员变更时，需要重新生成和分发多个密钥
     *
     * 我想简单版的实现：
     * 每个群聊维护一个会话session
     * 成员变更时，重新生成该密钥
     *
     * 群主创建群，有新成员加入时，新成员创建新的群聊会话session，并分发给所有老成员
     *
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @UniJSMethod(uiThread = false)
    fun createGroupSessionSimple(groupId: String,callback: UniJSCallback) {
        try {
            // 使用群聊ID和时间戳作为特殊的recipientId来创建会话
            val groupRecipientId = "group_$groupId"+ System.currentTimeMillis()
            // 1.创建发送者地址
            val selfAddress = SignalProtocolAddress(groupRecipientId.toString(), 1)
            // 2.创建持久化的 SignalProtocolStore
            val store = PersistentSignalProtocolStore(keyRepository, groupRecipientId)
            val sessionUtil = GroupSessionUtil(store)
            // 3.创建发送者密钥分发消息
            val distributionMessage = sessionUtil.createSenderKeyDistribution(groupId,selfAddress)
            // 序列化分发消息
            val serializedMessage = Base64.getEncoder().encodeToString(distributionMessage.serialize())
            // 返回序列化的分发消息
            Log.e(TAG,"创建群组会话分发者消息成功："+serializedMessage)
            callback.invoke(serializedMessage)
        }catch (ex: Exception){
            Log.e(TAG, "Error creating group session", ex)
            callback.invoke("创建群组会话失败: " + ex.message)
        }
    }

    /**
     * 创建群组发送者密钥
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @UniJSMethod(uiThread = false)
    fun createGroupSession(groupId: String,uniJSCallback: UniJSCallback) {
        try {
            val currentUserId = MMKV.defaultMMKV().decodeLong("currentUserId").toString()
            // 1.创建发送者地址
            selfAddress = SignalProtocolAddress(currentUserId.toString(), 1)
            // 2.创建持久化的 SignalProtocolStore
            val store = PersistentSignalProtocolStore(keyRepository, currentUserId)
            sessionUtil = GroupSessionUtil(store)
            // 3.创建发送者密钥分发消息
            val distributionMessage = sessionUtil.createSenderKeyDistribution(groupId,selfAddress)
            // 序列化分发消息
            val serializedMessage = Base64.getEncoder().encodeToString(distributionMessage.serialize())
            // 返回序列化的分发消息
            Log.e(TAG,"创建群组会话分发者消息成功："+serializedMessage)
            uniJSCallback.invoke(serializedMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating group session", e)
        }
    }

    /**
     * 处理接收到的群组发送者密钥
     * @param groupId 群组ID
     * @param senderId 发送者ID
     * @param encodedDistributionMessage Base64编码的分发消息
     * @return 处理是否成功
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @UniJSMethod(uiThread = false)
    fun processDistributionMessage(
        groupId: String,
        senderId: String,
        encodedDistributionMessage: String
    ) {
        try {
            val currentUserId = MMKV.defaultMMKV().decodeLong("currentUserId").toString()
            // 1.创建发送者地址
            selfAddress = SignalProtocolAddress(currentUserId.toString(), 1)
            // 2.创建持久化的 SignalProtocolStore
            val store = PersistentSignalProtocolStore(keyRepository, currentUserId)
            sessionUtil = GroupSessionUtil(store)
            // 3.解码分发消息
            val messageBytes = Base64.getDecoder().decode(encodedDistributionMessage)
            val distributionMessage = SenderKeyDistributionMessage(messageBytes)
            // 4.创建发送者地址
            val senderAddress = SignalProtocolAddress(senderId, 1)
            // 5.处理分发消息
            sessionUtil.processSenderKeyDistribution(groupId, senderAddress, distributionMessage)
        } catch (e: java.lang.Exception) {
            Log.e("GroupChat", "Error processing distribution message", e)
        }
    }

    /**
     * 加密群组消息
     * @param groupId 群组ID
     * @param message 明文消息
     * @return Base64编码的加密消息
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @UniJSMethod(uiThread = false)
    fun encryptGroupMessage(groupId: String?, message: String,callback: UniJSCallback) {
        try {
            Log.e(TAG,"群组要加密的消息：groupId:"+groupId+"msg:"+message)
            val currentUserId = MMKV.defaultMMKV().decodeLong("currentUserId").toString()
            // 1.创建发送者地址
            val selfAddress = SignalProtocolAddress(currentUserId.toString(), 1)
            // 2.创建持久化的 SignalProtocolStore
            val store = PersistentSignalProtocolStore(keyRepository, currentUserId)
            val cipherUtil = GroupCipherUtil(store)
            // 3.加密消息
            val plaintext: ByteArray? = message.toByteArray(StandardCharsets.UTF_8)
            val ciphertext = cipherUtil.encrypt(groupId, selfAddress, plaintext)
            // 编码加密后的消息
            val encryptMsg = Base64.getEncoder().encodeToString(ciphertext)
            Log.e(TAG, "加密后的群组消息："+encryptMsg)
            callback.invoke(encryptMsg)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Error encrypting group message", e)
            callback.invoke("")
        }
    }

    /**
     * 解密群组消息
     * @param groupId 群组ID
     * @param senderId 发送者ID
     * @param encodedMessage Base64编码的加密消息
     * @return 解密后的消息
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @UniJSMethod(uiThread = false)
    fun decryptGroupMessage(groupId: String?, senderId: String?, encodedMessage: String?,callback: UniJSCallback) {
        try {
            val currentUserId = MMKV.defaultMMKV().decodeLong("currentUserId").toString()
            // 解码加密消息
            val ciphertext = Base64.getDecoder().decode(encodedMessage)
            // 创建发送者地址
            val senderAddress = SignalProtocolAddress(senderId, 1)
            // 2.创建持久化的 SignalProtocolStore
            val store = PersistentSignalProtocolStore(keyRepository, currentUserId)
            val cipherUtil = GroupCipherUtil(store)
            // 解密消息
            val plaintext = cipherUtil.decrypt(groupId, senderAddress, ciphertext)
            // 转换为字符串
            val deMsg = kotlin.text.String(plaintext!!, StandardCharsets.UTF_8)
            Log.e(TAG, "解密后的群组消息："+deMsg)
            callback.invoke(deMsg)
        } catch (e: java.lang.Exception) {
            Log.e("GroupChat", "Error decrypting group message", e)
            callback.invoke("未正确解密消息")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createGroupOwnerSession(groupId: String) {
        Log.e(TAG, "创建群主的群组发送者密钥")
        executor.execute(Runnable {
            try {
                val currentUserId = MMKV.defaultMMKV().decodeLong("currentUserId").toString()
                // 1.创建发送者地址
                selfAddress = SignalProtocolAddress(currentUserId.toString(), 1)
                // 2.创建持久化的 SignalProtocolStore
                val store = PersistentSignalProtocolStore(keyRepository, currentUserId)
                sessionUtil = GroupSessionUtil(store)
                // 3.创建发送者密钥分发消息
                val distributionMessage = sessionUtil.createSenderKeyDistribution(groupId,selfAddress)
                // 序列化分发消息
                val serializedMessage = Base64.getEncoder().encodeToString(distributionMessage.serialize())
                //保存到自己的数据库表

            } catch (e: Exception) {
                Log.e(TAG, "Error creating group session", e)
            }
        })
    }

}