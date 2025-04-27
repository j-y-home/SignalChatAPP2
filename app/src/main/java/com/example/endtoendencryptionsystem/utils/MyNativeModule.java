package com.example.endtoendencryptionsystem.utils;


import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.endtoendencryptionsystem.entiy.database.PrivateMessage;
import com.example.endtoendencryptionsystem.model.KeyPairsMaker;
import com.example.endtoendencryptionsystem.model.PreKeyBundleMaker;
import com.example.endtoendencryptionsystem.model.StoreMaker;
import com.example.endtoendencryptionsystem.rsa.Entity;
import com.example.endtoendencryptionsystem.rsa.Session;

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

public class MyNativeModule extends UniModule {
    // 注册方法供 UniApp 调用（同步方法）
    @UniJSMethod(uiThread = true)
    public void showToast(String message) {
       // Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 带回调的注册方法 ：生成身份公钥和一次性密钥
     * 公钥 上传服务器
     * 私钥 本地sharePreference存储
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
     * 初始化获取aliceToBobSession
     */
    public Session initAliceToBobSession(String message, PreKeyBundle bobPreKeyBundle,PreKeyBundle alicePreKeyBundle) {
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
        KeyPairsMaker keyPairsMaker=snapshot.getValue(KeyPairsMaker.class);
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

            signalProtocolStore.storePreKey(alicePreKeyBundle.getPreKeyId(),preKeyRecord);
            signalProtocolStore.storeSignedPreKey(alicePreKeyBundle.getSignedPreKeyId(),signedPreKeyRecord);

            signalProtocolAddress=new SignalProtocolAddress(receiverUid,1);
            aliceToBobSession = new Session(signalProtocolStore,bobPreKeyBundle,signalProtocolAddress);
            return aliceToBobSession;
        }

        return null;
    }
    /**
     * 带回调的加密方法
     * message 待加密消息
     * receiverPreKeyBundleMakerJson 接收者的preKeyBundleMaker
     * sendPreKeyBundleMakerJson 发送者的preKeyBundleMaker
      */
    @UniJSMethod(uiThread = false)
    public void encrypt(String message,String receiverPreKeyBundleMakerJson,String senderPreKeyBundleMakerJson,String storeMaker,UniJSCallback callback) {
        Log.e("xxxx","调用加密方法："+message);
        PreKeyBundle bobPreKeyBundle,alicePreKeyBundle;
        String signalCipherText = "未正确加密消息";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //获取bobPreKeyBundle
            PreKeyBundleMaker bobPreKeyBundleMaker = JSONObject.parseObject(receiverPreKeyBundleMakerJson,PreKeyBundleMaker.class);
            bobPreKeyBundle= PreKeyBundleCreatorUtil.createPreKeyBundle(bobPreKeyBundleMaker);

            //获取alicePreKeyBundle
            PreKeyBundleMaker alicePreKeyBundleMaker = JSONObject.parseObject(senderPreKeyBundleMakerJson,PreKeyBundleMaker.class);
            alicePreKeyBundle=PreKeyBundleCreatorUtil.createPreKeyBundle(alicePreKeyBundleMaker);

            Session aliceToBobSession = initAliceToBobSession(message,bobPreKeyBundle,alicePreKeyBundle);
            PreKeySignalMessage toBobMessage = aliceToBobSession.encrypt(message);
            signalCipherText = Base64.getEncoder().encodeToString(toBobMessage.serialize());
            Log.e("xxxx","一系列算法加密后的消息："+signalCipherText);
        }
        callback.invoke(signalCipherText);
    }
    /**
     *   带回调的解密方法
     */
    @UniJSMethod(uiThread = false)
    public void decrypt(String msg,UniJSCallback callback) {
        String decryptedMsg = "未正确解密消息";
        byte[] ds= null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ds = Base64.getDecoder().decode(msg);
            PreKeySignalMessage toBobMessageDecrypt = null;
            try {
                toBobMessageDecrypt = new PreKeySignalMessage(ds);
                //previousCipherText.add(plainText);
                decryptedMsg = aliceToBobSession.decrypt(toBobMessageDecrypt);
            } catch (InvalidMessageException e) {
                throw new RuntimeException(e);
            } catch (InvalidVersionException e) {
                throw new RuntimeException(e);
            }
        }

        callback.invoke(decryptedMsg);
    }

    /**
     *   初始化会话
     *   1，获取B的相关密钥
     *   2，验证签名
     *   3，初始化会话并通过X3DH生成共享密钥
     */
    @UniJSMethod(uiThread = false)
    public void initializeSession(String preKeyBundle,UniJSCallback callback) {
        Log.e("xxxx","初始化会话模块");
        PreKeyBundleMaker preKeyBundleMaker = JSON.parseObject(preKeyBundle, PreKeyBundleMaker.class);
        //2， 验证签名
        boolean isValid = Curve.verifySignature(
                preKeyBundleMaker.getIdentityKey().getPublicKey(),
                preKeyBundleMaker.getSignedPreKeyPublic().serialize(),
                preKeyBundleMaker.getIdentityPreKeySignature()
        );

        if (!isValid) {
            throw new SecurityException("签名验证失败，密钥可能被篡改！");
        }
        callback.invoke(JSON.toJSON(list));
    }

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