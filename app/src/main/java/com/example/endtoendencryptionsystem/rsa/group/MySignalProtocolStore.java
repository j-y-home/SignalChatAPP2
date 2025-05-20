package com.example.endtoendencryptionsystem.rsa.group;

import android.content.Context;
import org.whispersystems.libsignal.IdentityKey;  
import org.whispersystems.libsignal.IdentityKeyPair;  
import org.whispersystems.libsignal.InvalidKeyIdException;  
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.groups.SenderKeyName;
import org.whispersystems.libsignal.groups.state.SenderKeyRecord;
import org.whispersystems.libsignal.state.PreKeyRecord;  
import org.whispersystems.libsignal.state.SessionRecord;  
import org.whispersystems.libsignal.state.SignedPreKeyRecord;  
  
import java.util.HashMap;  
import java.util.List;  
import java.util.Map;  
import java.util.UUID;  
  
public class MySignalProtocolStore implements   
    org.whispersystems.libsignal.state.IdentityKeyStore,  
    org.whispersystems.libsignal.state.PreKeyStore,  
    org.whispersystems.libsignal.state.SessionStore,  
    org.whispersystems.libsignal.state.SignedPreKeyStore,  
    org.whispersystems.libsignal.groups.state.SenderKeyStore {  
  
    private final Context context;  
    private final IdentityKeyPair identityKeyPair;  
    private final int registrationId;  
      
    // 内存存储，实际应用中应该使用数据库  
    private final Map<SignalProtocolAddress, SessionRecord> sessions = new HashMap<>();  
    private final Map<Integer, PreKeyRecord> preKeys = new HashMap<>();  
    private final Map<Integer, SignedPreKeyRecord> signedPreKeys = new HashMap<>();  
    private final Map<String, SenderKeyRecord> senderKeys = new HashMap<>();  
    private final Map<SignalProtocolAddress, IdentityKey> identities = new HashMap<>();  
      
    public MySignalProtocolStore(Context context, IdentityKeyPair identityKeyPair, int registrationId) {  
        this.context = context;  
        this.identityKeyPair = identityKeyPair;  
        this.registrationId = registrationId;  
    }  
      
    // IdentityKeyStore 实现  
    @Override  
    public IdentityKeyPair getIdentityKeyPair() {  
        return identityKeyPair;  
    }  
      
    @Override  
    public int getLocalRegistrationId() {  
        return registrationId;  
    }  
      
    @Override  
    public boolean saveIdentity(SignalProtocolAddress address, IdentityKey identityKey) {  
        IdentityKey existing = identities.get(address);  
        identities.put(address, identityKey);  
        return existing == null || !existing.equals(identityKey);  
    }  
      
    @Override  
    public boolean isTrustedIdentity(SignalProtocolAddress address, IdentityKey identityKey, Direction direction) {  
        IdentityKey trusted = identities.get(address);  
        return (trusted == null || trusted.equals(identityKey));  
    }  
      
    @Override  
    public IdentityKey getIdentity(SignalProtocolAddress address) {  
        return identities.get(address);  
    }  
      
    // SessionStore 实现  
    @Override  
    public SessionRecord loadSession(SignalProtocolAddress address) {  
        SessionRecord record = sessions.get(address);  
        return record == null ? new SessionRecord() : record;  
    }  
      
    @Override  
    public List<Integer> getSubDeviceSessions(String name) {  
        return null; // 简化实现  
    }  
      
    @Override  
    public void storeSession(SignalProtocolAddress address, SessionRecord record) {  
        sessions.put(address, record);  
    }  
      
    @Override  
    public boolean containsSession(SignalProtocolAddress address) {  
        return sessions.containsKey(address);  
    }  
      
    @Override  
    public void deleteSession(SignalProtocolAddress address) {  
        sessions.remove(address);  
    }  
      
    @Override  
    public void deleteAllSessions(String name) {  
        // 简化实现  
    }  
      
    // PreKeyStore 实现  
    @Override  
    public PreKeyRecord loadPreKey(int preKeyId) throws InvalidKeyIdException {  
        PreKeyRecord record = preKeys.get(preKeyId);  
        if (record == null) {  
            throw new InvalidKeyIdException("No such prekeyrecord!");  
        }  
        return record;  
    }  
      
    @Override  
    public void storePreKey(int preKeyId, PreKeyRecord record) {  
        preKeys.put(preKeyId, record);  
    }  
      
    @Override  
    public boolean containsPreKey(int preKeyId) {  
        return preKeys.containsKey(preKeyId);  
    }  
      
    @Override  
    public void removePreKey(int preKeyId) {  
        preKeys.remove(preKeyId);  
    }  
      
    // SignedPreKeyStore 实现  
    @Override  
    public SignedPreKeyRecord loadSignedPreKey(int signedPreKeyId) throws InvalidKeyIdException {  
        SignedPreKeyRecord record = signedPreKeys.get(signedPreKeyId);  
        if (record == null) {  
            throw new InvalidKeyIdException("No such signedprekeyrecord!");  
        }  
        return record;  
    }  
      
    @Override  
    public List<SignedPreKeyRecord> loadSignedPreKeys() {  
        return null; // 简化实现  
    }  
      
    @Override  
    public void storeSignedPreKey(int signedPreKeyId, SignedPreKeyRecord record) {  
        signedPreKeys.put(signedPreKeyId, record);  
    }  
      
    @Override  
    public boolean containsSignedPreKey(int signedPreKeyId) {  
        return signedPreKeys.containsKey(signedPreKeyId);  
    }  
      
    @Override  
    public void removeSignedPreKey(int signedPreKeyId) {  
        signedPreKeys.remove(signedPreKeyId);  
    }  
      
    // SenderKeyStore 实现

    @Override
    public void storeSenderKey(SenderKeyName senderKeyName, SenderKeyRecord record) {
        senderKeys.put(senderKeyName.getGroupId() + "::" + senderKeyName.getSender().getName(), record);
    }

    @Override
    public SenderKeyRecord loadSenderKey(SenderKeyName senderKeyName) {
        SenderKeyRecord record = senderKeys.get(senderKeyName.getGroupId() + "::" + senderKeyName.getSender().getName());
        return record == null ? new SenderKeyRecord() : record;
    }
}