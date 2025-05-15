package com.example.endtoendencryptionsystem.utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.example.endtoendencryptionsystem.ETEApplication.Companion.getInstance
import com.example.endtoendencryptionsystem.entiy.database.ChatConversation
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.PrivateMessage
import com.example.endtoendencryptionsystem.entiy.database.User
import com.example.endtoendencryptionsystem.model.KeyPairsMaker
import com.example.endtoendencryptionsystem.model.PreKeyBundleMaker
import com.example.endtoendencryptionsystem.model.StoreMaker
import com.example.endtoendencryptionsystem.repository.ChatRepository
import com.example.endtoendencryptionsystem.rsa.Entity
import com.example.endtoendencryptionsystem.rsa.Session
import com.tencent.mmkv.MMKV
import io.dcloud.feature.uniapp.annotation.UniJSMethod
import io.dcloud.feature.uniapp.bridge.UniJSCallback
import io.dcloud.feature.uniapp.common.UniModule
import org.whispersystems.libsignal.InvalidKeyException
import org.whispersystems.libsignal.SignalProtocolAddress
import org.whispersystems.libsignal.ecc.Curve
import org.whispersystems.libsignal.ecc.ECKeyPair
import org.whispersystems.libsignal.protocol.PreKeySignalMessage
import org.whispersystems.libsignal.state.PreKeyBundle
import org.whispersystems.libsignal.state.PreKeyRecord
import org.whispersystems.libsignal.state.SignalProtocolStore
import org.whispersystems.libsignal.state.SignedPreKeyRecord
import java.util.Base64
import java.util.Date
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
 * TODO 待处理bug1 ：删除了会话，websocket又发送了消息，导致数据库中又插入了已删除的数据。
 * TODO 待处理bug2 ：删除消息，数据库中删除了，但是UI页面未删除。
 * TODO 待处理问题1：除了主动发送的消息，其他消息如何添加messageId（比如：”你们已经成为好友啦“）
 * TODO 待处理问题2：图片，文件在数据库中如何存储。
 * TODO 待处理问题3：加密部分
 * TODO 待处理问题4：群聊
 * TODO 待处理问题5：撤回消息
 */
class MyNativeModule : UniModule() {
    // 注册方法供 UniApp 调用（同步方法）
    private val chatRepository = ChatRepository(getInstance()!!)
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val TAG: String = "MyNativeModule"

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
     * TODO  改到登录时获取密钥并上传到服务器
     * 因为注册时获取密钥并上传，逻辑会有问题：如果只是注册了，但是并未在该设备上登录（使用）
     * 那本设备存的私钥也没用，再次用另一台设备登录时，本地没有私钥也没法使用。所以以登录为准，
     * 登录才确定在该设备上使用。后续退出登录、或者登录状态失效的问题待定处理 TODO。
     * 带回调的注册方法 ：生成身份公钥和一次性密钥
     * 公钥 上传服务器
     * 私钥 本地sharePreference存储
     * uid:userId
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @UniJSMethod(uiThread = false)
    fun register(uid: String?, callback: UniJSCallback) {
        val random = Random()
        val preKeyId = random.nextInt(100)
        val signedPreKeyId = random.nextInt(100)
        var alice: Entity? = null
        try {
            Log.e("xxx", "注册走获取加密身份公钥等:"+uid)
            alice = Entity(preKeyId, signedPreKeyId, uid)
            val registrationId = alice.getStore().getLocalRegistrationId()
            val deviceId = alice.getPreKey().getDeviceId()
            val preKeyPublic =
                Base64.getEncoder().encodeToString(alice.getPreKey().getPreKey().serialize())
            val signedPreKeyPublic =
                Base64.getEncoder().encodeToString(alice.getPreKey().getSignedPreKey().serialize())
            val identityPreKeySignature =
                Base64.getEncoder().encodeToString(alice.getPreKey().getSignedPreKeySignature())
            val identityKey = Base64.getEncoder().encodeToString(
                alice.getStore().getIdentityKeyPair().getPublicKey().getPublicKey().serialize()
            )
            val preKeyBundleMaker = PreKeyBundleMaker(
                registrationId,
                deviceId,
                preKeyId,
                preKeyPublic,
                signedPreKeyId,
                signedPreKeyPublic,
                identityPreKeySignature,
                identityKey
            )
            //STORE
            val storeIdentityKey = Base64.getEncoder().encodeToString(
                alice.getStore().getIdentityKeyPair().getPublicKey().getPublicKey().serialize()
            )
            val storePrivateKey = Base64.getEncoder()
                .encodeToString(alice.getStore().getIdentityKeyPair().getPrivateKey().serialize())
            val storeMaker = StoreMaker(storeIdentityKey, storePrivateKey, registrationId)
            val data = JSONObject()
            Log.e(
                "xxx",
                "注册走获取加密身份公钥等:" + JSON.toJSON(preKeyBundleMaker) + "???" + JSON.toJSON(
                    storeMaker
                )
            )
            data.put("preKeyBundleMaker", JSON.toJSON(preKeyBundleMaker))
            data.put("storeMaker", JSON.toJSON(storeMaker))

            MMKV.defaultMMKV().encode("currentUserId", uid!!.toLong())
            MMKV.defaultMMKV().encode("preKeyBundleMaker", JSON.toJSONString(preKeyBundleMaker))
            MMKV.defaultMMKV().encode("storeMaker", JSON.toJSONString(storeMaker))
            //把这两个值传给uniapp
            callback.invoke(data)
        } catch (e: InvalidKeyException) {
            callback.invoke(null)
        }
    }

    /**
     * 添加好友
     * 该方法获取到了好友信息，但它的数据结构是user表的结构,需要转换
     */
    @UniJSMethod(uiThread = false)
    fun addFriend(friendJson: String?, callback: UniJSCallback) {
        Log.e(TAG, "接收到的好友信息：" + friendJson)
        try {
            var user = json.toObject<User>(friendJson.toString())
            val currentUserId = MMKV.defaultMMKV().decodeLong("currentUserId")
            Log.e(TAG, "currentUserId：" + currentUserId)
            val friend = Friend(
                userId = currentUserId,
                friendId = user.id.toLong(),
                friendNickName = user.nickName,
                friendHeadImage = user.headImage,
                createdTime = user.createdTime,
                preKeyBundleMaker = user.preKeyBundleMaker,
                storeMaker = user.storeMaker)
            Log.e(TAG, "要添加的Friend信息：" + JSONObject.toJSONString(friend))
            chatRepository.addFriend(friend)
            callback.invoke(true)
        } catch (ignored: Exception) {
            Log.e(TAG, "添加好友报错信息：" + ignored.message)
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
     * 相当复杂，我也没懂
     * 初始化获取aliceToBobSession
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    fun initAliceToBobSession(message: String?, receiverUid: String): Session? {
        Log.e("xxxx", "初始化initAliceToBobSession：" + message)
        /**
         * 每次的加密解密，都需要一个aliceToBobSession对象，要创建这个对象，需要一些密钥信息
         * signalProtocolStore
         * bobPreKeyBundle
         * signalProtocolAddress
         */
        val aliceToBobSession: Session?
        val signalProtocolStore: SignalProtocolStore?
        val signalProtocolAddress: SignalProtocolAddress?
        val privateKeys = MMKV.defaultMMKV().decodeString("privates")
        Log.e("xxxx", "本地存储的privates:" + privateKeys)
        val keyPairsMaker =
            JSONObject.parseObject<KeyPairsMaker>(privateKeys, KeyPairsMaker::class.java)
        var decodedPrivateKey: ByteArray? = null
        val bobPreKeyBundle: PreKeyBundle?
        val alicePreKeyBundle: PreKeyBundle?
        //1，根据receiverUid获取好友信息
        val friend = getFriendInfoById(receiverUid.toLong())
        if (friend != null) {
            //2, 获取bobPreKeyBundle
            var bobPreKeyBundleMaker = checkNotNull(json.toObject<PreKeyBundleMaker>(friend.preKeyBundleMaker.toString()))
            bobPreKeyBundle = PreKeyBundleCreatorUtil.createPreKeyBundle(bobPreKeyBundleMaker)

            //3,获取alicePreKeyBundle
            var alicePreKeyBundleMaker = checkNotNull(
                json.toObject<PreKeyBundleMaker>(MMKV.defaultMMKV().decodeString("preKeyBundleMaker").toString()))

            alicePreKeyBundle = PreKeyBundleCreatorUtil.createPreKeyBundle(alicePreKeyBundleMaker)

            //4,获取aliceStoreMaker
            val aliceStoreMaker = json.toObject<StoreMaker>(MMKV.defaultMMKV().decodeString("storeMaker")
                .toString())

            checkNotNull(keyPairsMaker)
            decodedPrivateKey = Base64.getDecoder().decode(keyPairsMaker.getPreKeyPairPrivateKey())
            val ecPrivateKey = Curve.decodePrivatePoint(decodedPrivateKey)
            val ecKeyPair = ECKeyPair(alicePreKeyBundle.getPreKey(), ecPrivateKey)
            val preKeyRecord = PreKeyRecord(alicePreKeyBundle.getPreKeyId(), ecKeyPair)

            val decodedSignedPrivateKey =
                Base64.getDecoder().decode(keyPairsMaker.getSignedPreKeySignaturePrivateKey())
            val signedPrivateKey = Curve.decodePrivatePoint(decodedSignedPrivateKey)
            val signedPreKeyPair = ECKeyPair(alicePreKeyBundle.getSignedPreKey(), signedPrivateKey)

            val signedPreKeyRecord = SignedPreKeyRecord(
                alicePreKeyBundle.getSignedPreKeyId(),
                keyPairsMaker.getTimestamp(),
                signedPreKeyPair,
                alicePreKeyBundle.getSignedPreKeySignature()
            )

            //初始化signalProtocolStore
            checkNotNull(aliceStoreMaker)
            signalProtocolStore =
                InMemorySignalProtocolStoreCreatorUtil.createStore(aliceStoreMaker)
            signalProtocolStore.storePreKey(alicePreKeyBundle.getPreKeyId(), preKeyRecord)
            signalProtocolStore.storeSignedPreKey(
                alicePreKeyBundle.getSignedPreKeyId(),
                signedPreKeyRecord
            )

            //初始化signalProtocolAddress userId和设备id组成“端”的唯一标识，目前只做单端，即一个用户只在一个设备上。
            signalProtocolAddress = SignalProtocolAddress(receiverUid, 1)
            aliceToBobSession = Session(signalProtocolStore, bobPreKeyBundle, signalProtocolAddress)
            return aliceToBobSession
        }

        return null
    }

    /**
     * 加密解密先放在一个方法里
     * encrypt 1:加密，0：解密
     * 带回调的加密解密方法
     * message 待加密消息
     * receiverPreKeyBundleMakerJson 接收者的preKeyBundleMaker
     * sendPreKeyBundleMakerJson 发送者的preKeyBundleMaker
     */
    @UniJSMethod(uiThread = false)
    fun encrypt(encrypt: Int, message: String, receiverId: String, callback: UniJSCallback) {
        Log.e(
            "xxxx",
            "当前操作类型：" + encrypt + "   接收者Id:" + receiverId + "  收到的消息：" + message
        )
        var signalCipherText: String? = "未正确加密消息"
        var decryptedMsg: String? = "未正确解密消息"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aliceToBobSession = initAliceToBobSession(message, receiverId)
            if (aliceToBobSession != null) {
                if (encrypt == 1) { //加密
                    val toBobMessage = aliceToBobSession.encrypt(message)
                    signalCipherText = Base64.getEncoder().encodeToString(toBobMessage.serialize())
                    Log.e("xxxx", "一系列算法加密后的消息：" + signalCipherText)
                    insertPrivateMessage(PrivateMessage(sendId = MMKV.defaultMMKV().decodeLong("currentUserId"),
                        recvId = receiverId.toLong(), content = message,sendTime = Date()))

                    var list = chatRepository.getAllMsgFromFriend(MMKV.defaultMMKV().decodeLong("currentUserId").toInt(), receiverId.toInt())
                    //遍历list
                    for (i in list.indices) {
                        Log.e("xxxx", "消息内容：" + list[i].content)
                    }
                    callback.invoke(signalCipherText)
                } else { //解密
                    val ds = Base64.getDecoder().decode(message)
                    var toBobMessageDecrypt: PreKeySignalMessage? = null
                    try {
                        toBobMessageDecrypt = PreKeySignalMessage(ds)
                        decryptedMsg = aliceToBobSession.decrypt(toBobMessageDecrypt)
                    } catch (ex: Exception) {
                    }
                    callback.invoke(decryptedMsg)
                }
            } else {
                Log.e("xxxx", "初始化Session失败：")
            }
        }
    }

    /**
     * 新增一条私聊消息
     */
    fun insertPrivateMessage(privateMessage: PrivateMessage) {
        chatRepository.insertPrivateMessage(privateMessage)
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


}