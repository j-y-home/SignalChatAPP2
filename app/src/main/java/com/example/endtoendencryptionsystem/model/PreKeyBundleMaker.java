package com.example.endtoendencryptionsystem.model;

/**
 * 注册到User信息里的
 */
public class PreKeyBundleMaker {
    int registrationId;
    int deviceId;
    int preKeyId;
    String preKeyPublic; //ECPublicKey
    int signedPreKeyId;
    String signedPreKeyPublic; //ECPublicKey
    String identityPreKeySignature; //byte []
    String identityKey;  //IdentityKey

    public PreKeyBundleMaker(int registrationId, int deviceId, int preKeyId, String preKeyPublic, int signedPreKeyId, String signedPreKeyPublic, String identityPreKeySignature,String identityKey) {
        this.registrationId = registrationId;
        this.deviceId = deviceId;
        this.preKeyId = preKeyId;
        this.preKeyPublic = preKeyPublic;
        this.signedPreKeyId = signedPreKeyId;
        this.signedPreKeyPublic = signedPreKeyPublic;
        this.identityPreKeySignature = identityPreKeySignature;
        this.identityKey = identityKey;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getPreKeyId() {
        return preKeyId;
    }

    public void setPreKeyId(int preKeyId) {
        this.preKeyId = preKeyId;
    }

    public String getPreKeyPublic() {
        return preKeyPublic;
    }

    public void setPreKeyPublic(String preKeyPublic) {
        this.preKeyPublic = preKeyPublic;
    }

    public int getSignedPreKeyId() {
        return signedPreKeyId;
    }

    public void setSignedPreKeyId(int signedPreKeyId) {
        this.signedPreKeyId = signedPreKeyId;
    }

    public String getSignedPreKeyPublic() {
        return signedPreKeyPublic;
    }

    public void setSignedPreKeyPublic(String signedPreKeyPublic) {
        this.signedPreKeyPublic = signedPreKeyPublic;
    }

    public String getIdentityPreKeySignature() {
        return identityPreKeySignature;
    }

    public void setIdentityPreKeySignature(String identityPreKeySignature) {
        this.identityPreKeySignature = identityPreKeySignature;
    }

    public String getIdentityKey() {
        return identityKey;
    }

    public void setIdentityKey(String identityKey) {
        this.identityKey = identityKey;
    }
}
