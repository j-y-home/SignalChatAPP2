package com.example.endtoendencryptionsystem.model;




public class KeyPairsMaker {
    String preKeyPairPrivateKey;
    String signedPreKeySignaturePrivateKey;
    long timestamp;

    public KeyPairsMaker(String preKeyPairPrivateKey, String signedPreKeySignaturePrivateKey, long timestamp) {
        this.preKeyPairPrivateKey = preKeyPairPrivateKey;
        this.signedPreKeySignaturePrivateKey = signedPreKeySignaturePrivateKey;
        this.timestamp = timestamp;
    }

    public String getPreKeyPairPrivateKey() {
        return preKeyPairPrivateKey;
    }

    public void setPreKeyPairPrivateKey(String preKeyPairPrivateKey) {
        this.preKeyPairPrivateKey = preKeyPairPrivateKey;
    }

    public String getSignedPreKeySignaturePrivateKey() {
        return signedPreKeySignaturePrivateKey;
    }

    public void setSignedPreKeySignaturePrivateKey(String signedPreKeySignaturePrivateKey) {
        this.signedPreKeySignaturePrivateKey = signedPreKeySignaturePrivateKey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
