package com.example.endtoendencryptionsystem.rsa.group;

import org.whispersystems.libsignal.SignalProtocolAddress;
import org.whispersystems.libsignal.groups.GroupSessionBuilder;
import org.whispersystems.libsignal.groups.SenderKeyName;
import org.whispersystems.libsignal.groups.state.SenderKeyStore;
import org.whispersystems.libsignal.protocol.SenderKeyDistributionMessage;

public class GroupSessionUtil {
    private final SenderKeyStore senderKeyStore;
      
    public GroupSessionUtil(SenderKeyStore senderKeyStore) {  
        this.senderKeyStore = senderKeyStore;  
    }  
      
    /**  
     * 创建群组会话并生成分发消息  
     * @param groupId 群组ID  
     * @param senderAddress 发送者地址  
     * @return 发送者密钥分发消息  
     */  
    public SenderKeyDistributionMessage createSenderKeyDistribution(String groupId, SignalProtocolAddress senderAddress) {
        SenderKeyName senderKeyName = new SenderKeyName(groupId, senderAddress);
        GroupSessionBuilder sessionBuilder = new GroupSessionBuilder(senderKeyStore);
        return sessionBuilder.create(senderKeyName);  
    }  
      
    /**  
     * 处理接收到的发送者密钥分发消息  
     * @param groupId 群组ID  
     * @param senderAddress 发送者地址  
     * @param distributionMessage 分发消息  
     */  
    public void processSenderKeyDistribution(String groupId, SignalProtocolAddress senderAddress,   
                                            SenderKeyDistributionMessage distributionMessage) {  
        SenderKeyName senderKeyName = new SenderKeyName(groupId, senderAddress);  
        GroupSessionBuilder sessionBuilder = new GroupSessionBuilder(senderKeyStore);  
        sessionBuilder.process(senderKeyName, distributionMessage);  
    }  
}