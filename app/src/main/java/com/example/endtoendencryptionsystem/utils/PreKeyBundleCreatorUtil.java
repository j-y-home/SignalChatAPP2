//package com.example.endtoendencryptionsystem.utils;
//
//import android.os.Build;
//
//import androidx.annotation.RequiresApi;
//
//import com.example.endtoendencryptionsystem.model.PreKeyBundleMaker;
//
//import org.whispersystems.libsignal.IdentityKey;
//import org.whispersystems.libsignal.InvalidKeyException;
//import org.whispersystems.libsignal.ecc.Curve;
//import org.whispersystems.libsignal.ecc.ECPublicKey;
//import org.whispersystems.libsignal.state.PreKeyBundle;
//
//import java.util.Base64;
//
//import lombok.SneakyThrows;
//import lombok.experimental.UtilityClass;
//
//@UtilityClass
//public class PreKeyBundleCreatorUtil {
//    @SneakyThrows
//    @RequiresApi(api=Build.VERSION_CODES.O)
//    public static PreKeyBundle createPreKeyBundle(PreKeyBundleMaker preKeyBundleMaker){
//        byte [] decodedPreKeyPublic=Base64.getDecoder().decode(preKeyBundleMaker.getPreKeyPublic());
//        ECPublicKey preKeyPublic= null;
//        try {
//            preKeyPublic = Curve.decodePoint(decodedPreKeyPublic,0);
//            byte [] decodedSignedPreKey=Base64.getDecoder().decode(preKeyBundleMaker.getSignedPreKeyPublic());
//            ECPublicKey signedPreKey=Curve.decodePoint(decodedSignedPreKey,0);
//            byte [] decodedSignedPreKeySignature= Base64.getDecoder().decode(preKeyBundleMaker.getIdentityPreKeySignature());
//            byte [] decodedIdentityKeysPublicKey= Base64.getDecoder().decode(preKeyBundleMaker.getIdentityKey());
//            ECPublicKey identityPublicKey=Curve.decodePoint(decodedIdentityKeysPublicKey,0);
//            IdentityKey identityKey=new IdentityKey(identityPublicKey);
//            return new PreKeyBundle(preKeyBundleMaker.getRegistrationId()
//                    ,preKeyBundleMaker.getDeviceId()
//                    ,preKeyBundleMaker.getPreKeyId()
//                    ,preKeyPublic
//                    ,preKeyBundleMaker.getSignedPreKeyId()
//                    ,signedPreKey
//                    ,decodedSignedPreKeySignature
//                    ,identityKey);
//        } catch (InvalidKeyException e) {
//            throw new RuntimeException(e);
//        }
//
//    }
//}
