package com.example.endtoendencryptionsystem.utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.example.endtoendencryptionsystem.ETEApplication.Companion.getInstance
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
 */
class MyNativeModule : UniModule() {
    // 注册方法供 UniApp 调用（同步方法）
    private val chatRepository = ChatRepository(getInstance()!!)
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val TAG: String = "MyNativeModule"

    @UniJSMethod(uiThread = true)
    fun showToast(message: String?) {
        // Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
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
     * 该方法获取到了好友信息，但它的数据结构是user表的结构，我如何把它存放到friend表中呢？
     */
    @UniJSMethod(uiThread = false)
    fun addFriend(friendJson: String?, callback: UniJSCallback) {
        Log.e("xxxx", "好友信息：" + friendJson)
        try {
            var user = json.toObject<User>(friendJson.toString())
            val currentUserId = MMKV.defaultMMKV().decodeLong("currentUserId")
            Log.e("xxxx", "currentUserId：" + currentUserId)
            val friend = Friend(
                userId = currentUserId,
                friendId = user.id!!.toLong(),
                friendNickName = user.nickName,
                friendHeadImage = user.headImage,
                createdTime = user.createdTime,
                preKeyBundleMaker = user.preKeyBundleMaker,
                storeMaker = user.storeMaker)
            Log.e("xxxx", "Friend信息：" + JSONObject.toJSONString(friend))
            chatRepository.addFriend(friend)
            callback.invoke(true)
        } catch (ignored: Exception) {
            Log.e("xxxx", "报错信息：" + ignored.message)
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
        Log.e("xxxx", list.size.toString() + "数据库好友表全部数据：" + JSONObject.toJSONString(list))
        var friend: Friend? = null
        Log.e("xxxx", "当前friendId"+friendId )
        friend = chatRepository.selectFriendsByFriendId(friendId)
        if(friend == null){
            Log.e("xxxx", "当前对话的好友信息查不到" )
        }
        Log.e("xxxx", "当前对话的好友信息：" + json.toJSONString(friend))
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
     * //TODO 加密解密先放在一个方法里
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
     * websokect收到消息后先解密，再插入到数据库
     */
    @UniJSMethod(uiThread = false)
    fun insertPrivateMessage( callback: UniJSCallback) {

    }


    //    /**
    //     *   初始化会话
    //     *   1，获取B的相关密钥
    //     *   2，验证签名
    //     *   3，初始化会话并通过X3DH生成共享密钥
    //     */
    //    @UniJSMethod(uiThread = false)
    //    public void initializeSession(String preKeyBundle,UniJSCallback callback) {
    //        Log.e("xxxx","初始化会话模块");
    //        PreKeyBundleMaker preKeyBundleMaker = JSON.parseObject(preKeyBundle, PreKeyBundleMaker.class);
    //        //2， 验证签名
    //        boolean isValid = Curve.verifySignature(
    //                preKeyBundleMaker.getIdentityKey().getPublicKey(),
    //                preKeyBundleMaker.getSignedPreKeyPublic().serialize(),
    //                preKeyBundleMaker.getIdentityPreKeySignature()
    //        );
    //
    //        if (!isValid) {
    //            throw new SecurityException("签名验证失败，密钥可能被篡改！");
    //        }
    //        callback.invoke(JSON.toJSON(list));
    //    }


    /**
     * 加载会话中的消息
     */
    fun getMessages(conversationId:Long,limit:Int,offset:Int,callback: UniJSCallback){

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
                Log.e("xxxx","获取到本地数据库消息："+json.toJSONString(result))
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
     * 读取本地数据库的消息:私聊
     * uniapp的消息对话框显示Android数据库里的消息数据
     * 现在uniapp的消息有两种来源
     * 1，服务器数据库的历史消息表（后期没用，因为存到服务器的都是加密的）
     * 2，websocket实时接收的消息
     * 在发送者这边来说，是不需要解密，直接读取Android数据库里的明文消息即可
     * 但是在接收者那边来说，需要解密，websocket收到消息，先解密再存到Android数据库中。
     *
     * 步骤：
     * 1，发送者发送了消息， 同时存到Android数据库私聊消息表（先不考虑撤回的情况）
     * 2，接收者收到消息，先解密，再存到Android数据库私聊消息表。
     * 3，uniapp的对话框中显示显示，从Android数据库中读取
     *
     */
    @UniJSMethod(uiThread = false)
    fun readLocalPrivateMsg(callback: UniJSCallback) {
        Log.e("xxxx", "获取Android数据库消息")
//        val list: MutableList<PrivateMessage?> = ArrayList<PrivateMessage?>()
//        for (i in 0..4) {
//            val privateMessage = PrivateMessage()
//            privateMessage.id = i.toLong()
//            privateMessage.content = "这是第" + i + "条私聊消息"
//            privateMessage.sendTime = Date()
//            privateMessage.sendId = 1
//            privateMessage.recvId = 23
//            privateMessage.type = 0
//            privateMessage.status = 3
//            list.add(privateMessage)
//        }
        callback.invoke(null)
    }
}