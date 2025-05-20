package com.example.endtoendencryptionsystem.rsa.group;

import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.groups.GroupCipher;  
import org.whispersystems.libsignal.groups.SenderKeyName;  
import org.whispersystems.libsignal.protocol.SenderKeyDistributionMessage;  
  
public class GroupMessageCrypto {  
    private final MySignalProtocolStore protocolStore;  
      
    public GroupMessageCrypto(MySignalProtocolStore protocolStore) {  
        this.protocolStore = protocolStore;
    }  
      
    public byte[] encryptGroupMessage(String groupId, SignalProtocolAddress sender, byte[] plaintext) {  
        SenderKeyName senderKeyName = new SenderKeyName(groupId, sender);  
        GroupCipher groupCipher = new GroupCipher(protocolStore, senderKeyName);  
        try {  
            return groupCipher.encrypt(plaintext);  
        } catch (Exception e) {  
            throw new RuntimeException("Failed to encrypt group message", e);  
        }  
    }  
      
    public byte[] decryptGroupMessage(String groupId, SignalProtocolAddress sender, byte[] ciphertext) {  
        SenderKeyName senderKeyName = new SenderKeyName(groupId, sender);  
        GroupCipher groupCipher = new GroupCipher(protocolStore, senderKeyName);  
        try {  
            return groupCipher.decrypt(ciphertext);  
        } catch (Exception e) {  
            throw new RuntimeException("Failed to decrypt group message", e);  
        }  
    }  
}