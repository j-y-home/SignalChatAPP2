package com.example.endtoendencryptionsystem.utils;


import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.endtoendencryptionsystem.ETEApplication;
import com.example.endtoendencryptionsystem.entiy.database.Friend;
import com.example.endtoendencryptionsystem.entiy.database.PrivateMessage;
import com.example.endtoendencryptionsystem.model.KeyPairsMaker;
import com.example.endtoendencryptionsystem.model.PreKeyBundleMaker;
import com.example.endtoendencryptionsystem.model.StoreMaker;
import com.example.endtoendencryptionsystem.repository.ChatRepository;
import com.example.endtoendencryptionsystem.rsa.Entity;
import com.example.endtoendencryptionsystem.rsa.Session;
import com.tencent.mmkv.MMKV;

import org.whispersystems.libsignal.InvalidKeyException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.InvalidVersionException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.ecc.Curve;
import org.whispersystems.libsignal.ecc.ECKeyPair;
import org.whispersystems.libsignal.ecc.ECPrivateKey;
import org.whispersystems.libsignal.protocol.PreKeySignalMessage;
import org.whispersystems.libsignal.state.PreKeyBundle;
import org.whispersystems.libsignal.state.PreKeyRecord;
import org.whispersystems.libsignal.state.SignalProtocolStore;
import org.whispersystems.libsignal.state.SignedPreKeyRecord;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Random;
import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.bridge.UniJSCallback;
import io.dcloud.feature.uniapp.common.UniModule;

/**
 在uniapp中改代码改的好费劲。
 就比如调用Android原生加解密方法的传参这里，我要在uniapp中获取到那些参数（好有的公钥等信息），就比较费劲，要改很多地方。
 我刚刚突然来了个灵感，但我不知道可不可行。
 就是uniapp既然人家是现成的做好的，我就尽量不在它里面改动。
 具体的加解密等所有相关的，我都放在Android数据库里，甚至必要时可以直接Android去调用接口。
 比如，用户加好友后，会有个好友列表，我把这个表数据存Android数据库。
 这样在Uniapp中点击某个好友聊天时，只需要传入该好友的id，来到Android这边找好友对应的信息（公钥等）去加密。
 问题一：好友的公钥等会定期更新吗，更新后，本地数据库要更新的问题。
 问题二：本地apk卸载后，好友信息也没了，可以做一键从服务器获取好友信息(其实同问题一的操作一样)。

 甚至我现在做好的登录时调用方法获取密钥，并调用接口更新到user，
 都可以把当前用户的信息存到Sharepreferce中（目前是存到uni的setStorage里面的）
 登录这块，后期再改，目前虽然实现了，但是我觉得有bug。

 最主要的是消息表，肯定是要存Android数据库的，因为服务器存的是加密的，存了也没用。

 */
public class MyNativeModule extends UniModule {
    // 注册方法供 UniApp 调用（同步方法）
    private ChatRepository chatRepository = new ChatRepository(ETEApplication.getInstance());
    @UniJSMethod(uiThread = true)
    public void showToast(String message) {
       // Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * TODO  改到登录时获取密钥并上传到服务器
     * 带回调的注册方法 ：生成身份公钥和一次性密钥
     * 公钥 上传服务器
     * 私钥 本地sharePreference存储
     * uid:userId
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @UniJSMethod(uiThread = false)
    public void register(String uid,UniJSCallback callback) {
        Random random=new Random();
        int preKeyId=random.nextInt(100);
        int signedPreKeyId=random.nextInt(100);
        Entity alice = null;
        try {
            Log.e("xxx","注册走获取加密身份公钥等");
            alice = new Entity(preKeyId,  signedPreKeyId, uid);
            int registrationId=alice.getStore().getLocalRegistrationId();
            int deviceId=alice.getPreKey().getDeviceId();
            String preKeyPublic=Base64.getEncoder().encodeToString(alice.getPreKey().getPreKey().serialize());
            String signedPreKeyPublic=Base64.getEncoder().encodeToString(alice.getPreKey().getSignedPreKey().serialize());
            String identityPreKeySignature=Base64.getEncoder().encodeToString(alice.getPreKey().getSignedPreKeySignature());
            String identityKey=Base64.getEncoder().encodeToString(alice.getStore().getIdentityKeyPair().getPublicKey().getPublicKey().serialize());
            PreKeyBundleMaker preKeyBundleMaker= new PreKeyBundleMaker(registrationId,deviceId,preKeyId,preKeyPublic,signedPreKeyId,signedPreKeyPublic,identityPreKeySignature,identityKey);
            //STORE
            String storeIdentityKey=Base64.getEncoder().encodeToString(alice.getStore().getIdentityKeyPair().getPublicKey().getPublicKey().serialize());
            String storePrivateKey=Base64.getEncoder().encodeToString(alice.getStore().getIdentityKeyPair().getPrivateKey().serialize());
            StoreMaker storeMaker=new StoreMaker(storeIdentityKey,storePrivateKey,registrationId);
            JSONObject data = new JSONObject();
            Log.e("xxx","注册走获取加密身份公钥等:"+JSON.toJSON(preKeyBundleMaker)+"???"+JSON.toJSON(storeMaker));
            data.put("preKeyBundleMaker", JSON.toJSON(preKeyBundleMaker));
            data.put("storeMaker", JSON.toJSON(storeMaker));
            //把这两个值传给uniapp
            callback.invoke(data);
        } catch (InvalidKeyException e) {
            callback.invoke(null);
        }

    }

    /**
     * 添加好友
     */
    @UniJSMethod(uiThread = false)
    public void addFriend(String friendJson,UniJSCallback callback) {
        Log.e("xxxx","好友信息："+friendJson);
        try {
            Friend friend= JSONObject.parseObject(friendJson, Friend.class);
            chatRepository.addFriend(friend);
            callback.invoke(true);
        }catch (Exception ignored){
            callback.invoke(false);
        }
    }

    /**
     * 初始化获取aliceToBobSession
     */
    public Session initAliceToBobSession(String message, String receiverUid,PreKeyBundle bobPreKeyBundle,PreKeyBundle alicePreKeyBundle,StoreMaker senderStoreMaker) {
        Log.e("xxxx","调用加密方法："+message);
        /**
         * 每次的加密解密，都需要一个aliceToBobSession对象，要创建这个对象，需要一些密钥信息
         * signalProtocolStore
         * bobPreKeyBundle
         * signalProtocolAddress
         */
        Session aliceToBobSession;
        SignalProtocolStore signalProtocolStore;
        SignalProtocolAddress signalProtocolAddress;
        String privateKeys = MMKV.defaultMMKV().decodeString("privates");
        KeyPairsMaker keyPairsMaker= JSONObject.parseObject(privateKeys, KeyPairsMaker.class);
        byte[] decodedPrivateKey= null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            decodedPrivateKey = Base64.getDecoder().decode(keyPairsMaker.getPreKeyPairPrivateKey());
            ECPrivateKey ecPrivateKey=Curve.decodePrivatePoint(decodedPrivateKey);
            ECKeyPair ecKeyPair=new ECKeyPair(alicePreKeyBundle.getPreKey(),ecPrivateKey);
            PreKeyRecord preKeyRecord=new PreKeyRecord(alicePreKeyBundle.getPreKeyId(),ecKeyPair);

            byte[] decodedSignedPrivateKey=Base64.getDecoder().decode(keyPairsMaker.getSignedPreKeySignaturePrivateKey());
            ECPrivateKey signedPrivateKey=Curve.decodePrivatePoint(decodedSignedPrivateKey);
            ECKeyPair signedPreKeyPair=new ECKeyPair(alicePreKeyBundle.getSignedPreKey(),signedPrivateKey);

            SignedPreKeyRecord signedPreKeyRecord=new SignedPreKeyRecord(
                    alicePreKeyBundle.getSignedPreKeyId(),keyPairsMaker.getTimestamp(),signedPreKeyPair,alicePreKeyBundle.getSignedPreKeySignature());

            //初始化signalProtocolStore
            signalProtocolStore=InMemorySignalProtocolStoreCreatorUtil.createStore(senderStoreMaker);
            signalProtocolStore.storePreKey(alicePreKeyBundle.getPreKeyId(),preKeyRecord);
            signalProtocolStore.storeSignedPreKey(alicePreKeyBundle.getSignedPreKeyId(),signedPreKeyRecord);

            //初始化signalProtocolAddress userId和设备id组成“端”的唯一标识，目前只做单端，即一个用户只在一个设备上。
            signalProtocolAddress=new SignalProtocolAddress(receiverUid,1);
            aliceToBobSession = new Session(signalProtocolStore,bobPreKeyBundle,signalProtocolAddress);
            return aliceToBobSession;
        }

        return null;
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
    public void encrypt(int encrypt,String message,String receiverId,String receiverPreKeyBundleMakerJson,String senderPreKeyBundleMakerJson,String senderStoreMaker,UniJSCallback callback) {
        Log.e("xxxx","调用加密方法："+message);
        PreKeyBundle bobPreKeyBundle,alicePreKeyBundle;
        String signalCipherText = "未正确加密消息";
        String decryptedMsg = "未正确解密消息";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //获取bobPreKeyBundle
            PreKeyBundleMaker bobPreKeyBundleMaker = JSONObject.parseObject(receiverPreKeyBundleMakerJson,PreKeyBundleMaker.class);
            bobPreKeyBundle= PreKeyBundleCreatorUtil.createPreKeyBundle(bobPreKeyBundleMaker);

            //获取alicePreKeyBundle
            PreKeyBundleMaker alicePreKeyBundleMaker = JSONObject.parseObject(senderPreKeyBundleMakerJson,PreKeyBundleMaker.class);
            alicePreKeyBundle=PreKeyBundleCreatorUtil.createPreKeyBundle(alicePreKeyBundleMaker);

            //获取aliceStoreMaker
            StoreMaker aliceStoreMaker = JSONObject.parseObject(senderStoreMaker,StoreMaker.class);

            Session aliceToBobSession = initAliceToBobSession(message,receiverId,bobPreKeyBundle,alicePreKeyBundle,aliceStoreMaker);

            if(encrypt == 1){//加密
                PreKeySignalMessage toBobMessage = aliceToBobSession.encrypt(message);
                signalCipherText = Base64.getEncoder().encodeToString(toBobMessage.serialize());
                Log.e("xxxx","一系列算法加密后的消息："+signalCipherText);
                callback.invoke(signalCipherText);
            }else{//解密
                byte[] ds = Base64.getDecoder().decode(message);
                PreKeySignalMessage toBobMessageDecrypt = null;
                decryptedMsg = aliceToBobSession.decrypt(toBobMessageDecrypt);
                callback.invoke(decryptedMsg);
            }
        }

    }
//    /**
//     *   带回调的解密方法
//     */
//    @UniJSMethod(uiThread = false)
//    public void decrypt(String msg,UniJSCallback callback) {
//        String decryptedMsg = "未正确解密消息";
//        byte[] ds= null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            ds = Base64.getDecoder().decode(msg);
//            PreKeySignalMessage toBobMessageDecrypt = null;
//            try {
//                toBobMessageDecrypt = new PreKeySignalMessage(ds);
//                //previousCipherText.add(plainText);
//                Session aliceToBobSession = initAliceToBobSession(message,receiverId,bobPreKeyBundle,alicePreKeyBundle,aliceStoreMaker);
//                decryptedMsg = aliceToBobSession.decrypt(toBobMessageDecrypt);
//            } catch (InvalidMessageException e) {
//                throw new RuntimeException(e);
//            } catch (InvalidVersionException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        callback.invoke(decryptedMsg);
//    }

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
     *   读取本地数据库的消息:私聊
     */
    @UniJSMethod(uiThread = false)
    public void readLocalPrivateMsg(UniJSCallback callback) {
        Log.e("xxxx","获取Android数据库消息");
        List<PrivateMessage> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PrivateMessage privateMessage = new PrivateMessage();
            privateMessage.setId(Long.valueOf(i));
            privateMessage.setContent("这是第"+i+"条私聊消息");
            privateMessage.setSendTime(new Date());
            privateMessage.setSendId(Long.valueOf(1));
            privateMessage.setRecvId(Long.valueOf(23));
            privateMessage.setType(0);
            privateMessage.setStatus(3);
            list.add(privateMessage);
        }
        callback.invoke(JSON.toJSON(list));
    }
}