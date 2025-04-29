package com.example.endtoendencryptionsystem.model;

/**
 * 注册到User信息里的
 */
public class StoreMaker {
    //InMemorySignalProtocolStore(IdentityKeyPair identityKeyPair, int registrationId)
    //IdentityKeyPair(IdentityKey publicKey, ECPrivateKey privateKey)
    //public IdentityKey(ECPublicKey publicKey)
    String storeIdentityKey;
    String storePrivateKey;
    int registrationId;

    public StoreMaker() {
    }
    public StoreMaker(String storeIdentityKey, String storePrivateKey, int registrationId) {
        this.storeIdentityKey = storeIdentityKey;
        this.storePrivateKey = storePrivateKey;
        this.registrationId = registrationId;
    }

    public String getStoreIdentityKey() {
        return storeIdentityKey;
    }

    public void setStoreIdentityKey(String storeIdentityKey) {
        this.storeIdentityKey = storeIdentityKey;
    }

    public String getStorePrivateKey() {
        return storePrivateKey;
    }

    public void setStorePrivateKey(String storePrivateKey) {
        this.storePrivateKey = storePrivateKey;
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }
}
