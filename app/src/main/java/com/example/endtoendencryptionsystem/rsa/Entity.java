//package com.example.endtoendencryptionsystem.rsa;
//
//
//import android.content.SharedPreferences;
//import android.os.Build;
//
//import androidx.annotation.RequiresApi;
//
//
//import com.alibaba.fastjson.JSONObject;
//import com.example.endtoendencryptionsystem.model.KeyPairsMaker;
//import com.tencent.mmkv.MMKV;
//
//import org.whispersystems.libsignal.IdentityKey;
//import org.whispersystems.libsignal.IdentityKeyPair;
//import org.whispersystems.libsignal.InvalidKeyException;
//import org.whispersystems.libsignal.SignalProtocolAddress;
//import org.whispersystems.libsignal.ecc.Curve;
//import org.whispersystems.libsignal.ecc.ECKeyPair;
//import org.whispersystems.libsignal.ecc.ECPublicKey;
//import org.whispersystems.libsignal.state.PreKeyBundle;
//import org.whispersystems.libsignal.state.PreKeyRecord;
//import org.whispersystems.libsignal.state.SignalProtocolStore;
//import org.whispersystems.libsignal.state.SignedPreKeyRecord;
//import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;
//import org.whispersystems.libsignal.util.KeyHelper;
//
//import java.util.Base64;
//public class Entity {
//    /**
//     * Signal 协议的存储接口，用于存储密钥对、预密钥等信息。这里使用的是 InMemorySignalProtocolStore，它是一个内存中的实现。
//     */
//    private final SignalProtocolStore store;
//    /**
//     * Signal 协议中的预密钥包（PreKeyBundle），包含设备的预密钥、签名预密钥和身份密钥，用于与其他设备建立安全会话。
//     */
//    private final PreKeyBundle preKey;
//    /**
//     * Signal 协议地址，标识用户的唯一身份（由用户地址和设备 ID 组成）。
//     */
//    private final SignalProtocolAddress address;
//
//    /**
//     *
//     * @param preKeyId:预密钥的 ID。
//     * @param signedPreKeyId:签名预密钥的 ID。
//     * @param address : 用户的地址（通常是用户的唯一标识，如用户名或手机号）。
//     * @throws InvalidKeyException
//     */
//    @RequiresApi(api=Build.VERSION_CODES.O)
//    public Entity(int preKeyId,int signedPreKeyId,String address)
//            throws InvalidKeyException
//    {
//        /**
//         * 1,初始化 Signal 协议地址:
//         * 创建一个 Signal 协议地址，1 表示设备 ID。
//         */
//        this.address = new SignalProtocolAddress(address, 1);
//        /**
//         * 2,初始化 Signal 协议存储:
//         * 创建一个 InMemorySignalProtocolStore 实例，用于存储密钥对和预密钥。
//         */
//        this.store = new InMemorySignalProtocolStore(
//                KeyHelper.generateIdentityKeyPair(),
//                KeyHelper.generateRegistrationId(false));
//        /**
//         * 3,生成身份密钥对和注册 ID。
//         */
//        IdentityKeyPair identityKeyPair = store.getIdentityKeyPair();
//        int registrationId = store.getLocalRegistrationId();
//
//        /**
//         * 4,生成密钥对:
//         * 生成预密钥对和签名预密钥对。
//         * 使用 Curve.generateKeyPair() 方法生成密钥对。
//         */
//        ECKeyPair preKeyPair = Curve.generateKeyPair();
//        ECKeyPair signedPreKeyPair = Curve.generateKeyPair();
//        int deviceId =1;
//        long timestamp = System.currentTimeMillis();
//
//        /**
//         * 5,签名签名预密钥:
//         * 使用身份密钥对的私钥对签名预密钥的公钥进行签名，确保签名预密钥的真实性。
//         */
//        byte[] signedPreKeySignature = Curve.calculateSignature(
//                identityKeyPair.getPrivateKey(),
//                signedPreKeyPair.getPublicKey().serialize());
//
//        IdentityKey identityKey = identityKeyPair.getPublicKey();
//        ECPublicKey preKeyPublic = preKeyPair.getPublicKey();
//        String preKeyPairPrivateKey=Base64.getEncoder().encodeToString(preKeyPair.getPrivateKey().serialize());
//        String signedPrivateKey=Base64.getEncoder().encodeToString(signedPreKeyPair.getPrivateKey().serialize());
//
//        /**
//         * 6,构建密钥对信息:
//         * 将生成的密钥对信息（如私钥、签名等）封装到 KeyPairsMaker 对象中。
//         */
//        KeyPairsMaker keyPairsMaker=new KeyPairsMaker(preKeyPairPrivateKey,
//                signedPrivateKey,timestamp );
//        /**
//         * 6.1 TODO （私钥相关）保存到本地
//         */
//        MMKV.defaultMMKV().encode("privates", JSONObject.toJSONString(keyPairsMaker));
//      //  FirebaseDatabase.getInstance().getReference("privates").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(keyPairsMaker);
//        ECPublicKey signedPreKeyPublic = signedPreKeyPair.getPublicKey();
//
//        /**
//         * 7,创建预密钥包:
//         * 构建 PreKeyBundle，包含预密钥、签名预密钥和身份密钥，用于与其他设备建立安全通信。
//         */
//        this.preKey = new PreKeyBundle(
//                registrationId,
//                deviceId,
//                preKeyId,
//                preKeyPublic,
//                signedPreKeyId,
//                signedPreKeyPublic,
//                signedPreKeySignature,
//                identityKey);
//
//        PreKeyRecord preKeyRecord = new PreKeyRecord(preKey.getPreKeyId(), preKeyPair);
//        SignedPreKeyRecord signedPreKeyRecord = new SignedPreKeyRecord(
//                signedPreKeyId, timestamp, signedPreKeyPair, signedPreKeySignature);
//        /**
//         * 8,存储密钥:
//         * 将预密钥和签名预密钥存储到 Signal 协议存储中，以便后续使用。
//         */
//        store.storePreKey(preKeyId, preKeyRecord);
//        store.storeSignedPreKey(signedPreKeyId, signedPreKeyRecord);
//    }
//
//    public SignalProtocolStore getStore() {
//        return store;
//    }
//
//    public PreKeyBundle getPreKey() {
//        return preKey;
//    }
//
//    public SignalProtocolAddress getAddress() {
//        return address;
//    }
//}
