//package com.example.endtoendencryptionsystem.rsa.group;
//
//import org.whispersystems.libsignal.SignalProtocolAddress;
//import org.whispersystems.libsignal.groups.GroupSessionBuilder;
//import org.whispersystems.libsignal.groups.state.SenderKeyRecord;
//import org.whispersystems.libsignal.groups.SenderKeyName;
//import org.whispersystems.libsignal.protocol.SenderKeyDistributionMessage;
//import org.whispersystems.libsignal.state.impl.InMemorySignalProtocolStore;
//
//public class GroupSessionManager {
//    private final InMemorySignalProtocolStore protocolStore;
//    private final SignalProtocolAddress selfAddress;
//
//    public GroupSessionManager(InMemorySignalProtocolStore protocolStore, SignalProtocolAddress selfAddress) {
//        this.protocolStore = protocolStore;
//        this.selfAddress = selfAddress;
//    }
//
//    public SenderKeyDistributionMessage createSenderKeyDistributionMessage(String groupId) {
//        String distributionId = DistributionIdManager.getOrCreateDistributionId(groupId);
//        SenderKeyName senderKeyName = new SenderKeyName(groupId, selfAddress);
//        GroupSessionBuilder groupSessionBuilder = new GroupSessionBuilder(protocolStore);
//        return groupSessionBuilder.create(senderKeyName);
//    }
//
//    public void processSenderKeyDistributionMessage(
//            String groupId,
//            SignalProtocolAddress sender,
//            SenderKeyDistributionMessage senderKeyDistributionMessage) {
//        SenderKeyName senderKeyName = new SenderKeyName(groupId, sender);
//        GroupSessionBuilder groupSessionBuilder = new GroupSessionBuilder(protocolStore);
//        groupSessionBuilder.process(senderKeyName, senderKeyDistributionMessage);
//    }
//}