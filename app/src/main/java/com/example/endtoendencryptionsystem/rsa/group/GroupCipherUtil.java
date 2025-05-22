package com.example.endtoendencryptionsystem.rsa.group;

import org.whispersystems.libsignal.DuplicateMessageException;
import org.whispersystems.libsignal.InvalidMessageException;
import org.whispersystems.libsignal.LegacyMessageException;
import org.whispersystems.libsignal.NoSessionException;
import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.groups.GroupCipher;
import org.whispersystems.libsignal.groups.SenderKeyName;
import org.whispersystems.libsignal.groups.state.SenderKeyStore;

public class GroupCipherUtil {
    private final SenderKeyStore senderKeyStore;
      
    public GroupCipherUtil(SenderKeyStore senderKeyStore) {  
        this.senderKeyStore = senderKeyStore;  
    }  
      
    /**  
     * 加密群组消息  
     * @param groupId 群组ID  
     * @param senderAddress 发送者地址  
     * @param plaintext 明文消息  
     * @return 加密后的消息  
     */  
    public byte[] encrypt(String groupId, SignalProtocolAddress senderAddress, byte[] plaintext)
            throws NoSessionException, InvalidMessageException {
        SenderKeyName senderKeyName = new SenderKeyName(groupId, senderAddress);
        GroupCipher groupCipher = new GroupCipher(senderKeyStore, senderKeyName);
        return groupCipher.encrypt(plaintext);  
    }  
      
    /**  
     * 解密群组消息  
     * @param groupId 群组ID  
     * @param senderAddress 发送者地址  
     * @param ciphertext 密文消息  
     * @return 解密后的消息  
     */  
    public byte[] decrypt(String groupId, SignalProtocolAddress senderAddress, byte[] ciphertext)   
            throws NoSessionException, InvalidMessageException, LegacyMessageException, DuplicateMessageException {
        SenderKeyName senderKeyName = new SenderKeyName(groupId, senderAddress);  
        GroupCipher groupCipher = new GroupCipher(senderKeyStore, senderKeyName);  
        return groupCipher.decrypt(ciphertext);  
    }  
}