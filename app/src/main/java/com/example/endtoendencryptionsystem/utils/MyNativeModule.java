package com.example.endtoendencryptionsystem.utils;


import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.endtoendencryptionsystem.entiy.database.PrivateMessage;
import com.example.endtoendencryptionsystem.model.PreKeyBundleMaker;
import com.example.endtoendencryptionsystem.model.StoreMaker;
import com.example.endtoendencryptionsystem.rsa.Entity;

import org.whispersystems.libsignal.InvalidKeyException;

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
            Log.e("xxx","注册获取公钥失败");
            callback.invoke(null);
        }

    }

    /**
     * 带回调的加密方法
      */
    @UniJSMethod(uiThread = false)
    public void encrypt(String message,UniJSCallback callback) {
        Log.e("xxxx","调用加密方法："+message);
//        SessionCipher sessionCipher = new SessionCipher(protocolStore, address);
//        try {
//            CiphertextMessage ciphertext = sessionCipher.encrypt(message.getBytes());
//            callback.invoke(ciphertext.serialize());
//        } catch (UntrustedIdentityException e) {
//            callback.invoke(message);
//        }
        callback.invoke(message);
    }
    /**
     *   带回调的解密方法
     */
    @UniJSMethod(uiThread = false)
    public void decrypt(String msg,UniJSCallback callback) {
//        // 接收 ciphertextBytes（字节流）
//        CiphertextMessage ciphertext = CiphertextMessage.deserialize(ciphertextBytes);
//        byte[] plaintext = sessionCipher.decrypt(ciphertext);
//        String decryptedMessage = new String(plaintext);
        callback.invoke(msg);
    }


    /**
     *   读取本地数据库的消息:私聊
     */
    @UniJSMethod(uiThread = false)
    public void readLocalPrivateMsg(UniJSCallback callback) {
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