package com.example.endtoendencryptionsystem.utils

import android.content.Context
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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

object EncryptionUtil {
    private val chatRepository = ChatRepository(getInstance()!!)
    private val keyRepository = KeyRepository(getInstance()!!)
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val TAG: String = "EncryptionUtil"
    private var senderKeyStore: SenderKeyStore? = null
    private lateinit var cipherUtil: GroupCipherUtil
    private lateinit var sessionUtil: GroupSessionUtil
    private lateinit var selfAddress: SignalProtocolAddress

    /**
     * 改进后的注册密钥方法（登录后注册，现在是手动点击我的页面的注册按钮）
     * 返回的数据结构：
     * {"identityKey":"BasPxq4TWBFD3tzMg2uou81fs5jY3Re2U+9Z73gB4jMF","registrationId":7736,"signedPreKeys":[{"publicKey":"BWCEIBQ9CADQYlLYPPHy9sc4qXu1FPLfol5+fe7S8iN7","keyId":3,"timestamp":1747905064570,"signature":"1jFq1HIbUDrv6w1lWT2mCJj2nW7WkCK4ibsnqMJM+46YOrdwrl/Zg99QykQc+lnk3393eGLhkQlJ3VlNw3T2DA=="}],"preKeys":[{"publicKey":"BSrHexwl3KpkdoOhDnJGORCQx1W2ZGddm+/Iru32v7oV","keyId":69}]}
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    fun registerKey(): String {
        try {
            val currentUserId = MMKV.defaultMMKV().decodeInt("userId").toString()
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
            return  json.toJSONString(data)
        } catch (e: Exception) {
            Log.e("SignalError", "Key generation failed", e)
            return ""
        }
    }



    /**
     * 改进后的建立私聊初始化会话方法（加入好友立马建立初始化session会话，添加会话信息到数据库表）
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun initPrivateSession(friendId: String, preKeyBundleJson: String) {
        try {
            val currentUserId = MMKV.defaultMMKV().decodeInt("userId").toString()
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
//                //TODO 先删除，再创建（好友密钥有更新时，拉取到最新好友信息后，这样操作）
//                store.deleteSession(friendAddress)
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
    fun encryptPrivateMessage(friendId: String, message: String):String ?{
        try {
            val currentUserId = MMKV.defaultMMKV().decodeInt("userId").toString()
            // 1. 创建持久化的 SignalProtocolStore
            val store = PersistentSignalProtocolStore(keyRepository, currentUserId)
            // 2. 创建好友的 SignalProtocolAddress
            val friendAddress = SignalProtocolAddress(friendId, 1)
            // 3. 检查会话是否存在
            if (!store.containsSession(friendAddress)) {
                Log.e("Encrypt", "No session exists for friend: $friendId")
                return null
            }
            // 4. 创建SessionCipher进行加密
            val sessionCipher = SessionCipher(store, friendAddress)
            val ciphertext = sessionCipher.encrypt(message.toByteArray(StandardCharsets.UTF_8));
            // 5. 将加密结果编码为Base64
            val encryptedBase64 = Base64.getEncoder().encodeToString( ciphertext.serialize())
            return encryptedBase64

        } catch (e: Exception) {
            Log.e("Encrypt", "Failed to encrypt message for friend: $friendId", e)
            return null
        }
    }

    /**
     * 私聊消息解密
     * @param friendId 好友ID
     * @param encryptedMessage Base64编码的加密消息
     * @param callback 回调函数
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun decryptPrivateMessage(friendId: String, encryptedMessage: String) :String {
        try {
            val currentUserId = MMKV.defaultMMKV().decodeInt("userId").toString()
            // 1. 创建持久化的 SignalProtocolStore
            val store = PersistentSignalProtocolStore(keyRepository, currentUserId)
            // 2. 创建好友的 SignalProtocolAddress
            val friendAddress = SignalProtocolAddress(friendId, 1)
            // 3. 检查会话是否存在
            if (!store.containsSession(friendAddress)) {
                Log.e("Decrypt", "No session exists for friend: $friendId")
                return "会话不存在"
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
            return decryptedMessage
        } catch (e: Exception) {
            Log.e("Decrypt", "Failed to decrypt message for friend: $friendId", e)
            return "解密失败"
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
     * 简单版不行，还是需要signal本身的逻辑。
     *
     * 目前剩余的功能：
     * 群聊部分：
     * 1，成员退出群||被踢出群，目前是只在群组表标记quit=1，不显示该群组。本地群聊和群聊会话表都保留，可看，但不可发消息了。
     * 2，成员删除群聊（消息页的会话长按删除事件吗，会提示），在本地清空群聊表和该条群聊会话表
     * 3，群主解散群，如何处理成员本地的数据库表？
     * 4，不管是一天更新一次密钥，还是有新成员加入时更新密钥，都要确保如何让不在线成员也能生成并分发新的密钥。
     *
     * 私聊部分：
     * 1，图片和文件的处理（）。
     * 2，好友本地数据库表的处理。
     * 3，私聊会话表的处理。
     *
     */
    @RequiresApi(Build.VERSION_CODES.O)
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
     * //TODO 解散群聊  把 该群组的所有密钥删除
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun createGroupSession(groupId: String, type: String, uniJSCallback: UniJSCallback) {
        runBlocking {
            try {
                val currentUserId = MMKV.defaultMMKV().decodeInt("userId").toString()

                if(type == "退出"){
                    //删除发送者密钥。当有成员退出时，老成员需要更新密钥。需要删除再重新创建，否则不会创建。
                    withContext(Dispatchers.IO) {
                        keyRepository.deleteSenderKey("$groupId::$currentUserId::1")
                    }
                    //TODO 后期加入该退出成员的密钥删除
                }

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

    }

    /**
     * 处理接收到的群组发送者密钥
     * @param groupId 群组ID
     * @param senderId 发送者ID
     * @param encodedDistributionMessage Base64编码的分发消息
     * @return 处理是否成功
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun processDistributionMessage(
        groupId: String,
        senderId: String,
        encodedDistributionMessage: String
    ) {
        try {
            val currentUserId = MMKV.defaultMMKV().decodeInt("userId").toString()
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
    fun encryptGroupMessage(groupId: String?, message: String,callback: UniJSCallback) {
        try {
            Log.e(TAG,"群组要加密的消息：groupId:"+groupId+"msg:"+message)
            val currentUserId = MMKV.defaultMMKV().decodeInt("userId").toString()
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
    fun decryptGroupMessage(groupId: String?, senderId: String?, encodedMessage: String?,callback: UniJSCallback) {
        try {
            val currentUserId = MMKV.defaultMMKV().decodeInt("userId").toString()
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
                val currentUserId = MMKV.defaultMMKV().decodeInt("userId").toString()
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