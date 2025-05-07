package com.example.endtoendencryptionsystem.entiy.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;  
import androidx.room.Index;  
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.Nullable;

@Entity(  
    tableName = "group_chat_message",  
    foreignKeys = @ForeignKey(  
        entity = ChatConversation.class,  
        parentColumns = "id",  
        childColumns = "conversationId",  
        onDelete = ForeignKey.CASCADE  
    ),  
    indices = {  
        @Index("conversationId"),  
        @Index("serverMsgId"),  
        @Index("sendTime")  
    }  
)  
public class GroupChatMessage {  
    @PrimaryKey(autoGenerate = true)  
    private long id;
    @Nullable
    private long serverMsgId;
    @Nullable
    private String tmpId;
    @Nullable
    private long conversationId;
    @Nullable
    private long sendId;
    @Nullable
    private long groupId;
    @Nullable
    private String sendNickName;
    @Nullable
    private String content;
    @Nullable
    private long sendTime;
    @Nullable
    private boolean selfSend;
    @Nullable
    private int type;
    @Nullable
    private int status;
    @Nullable
    private int readedCount;
    @Nullable
    private String loadStatus;
    @Nullable
    private String atUserIds;
    @Nullable
    private boolean receipt;
    @Nullable
    private boolean receiptOk;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getServerMsgId() {
        return serverMsgId;
    }

    public void setServerMsgId(long serverMsgId) {
        this.serverMsgId = serverMsgId;
    }

    public String getTmpId() {
        return tmpId;
    }

    public void setTmpId(String tmpId) {
        this.tmpId = tmpId;
    }

    public long getConversationId() {
        return conversationId;
    }

    public void setConversationId(long conversationId) {
        this.conversationId = conversationId;
    }

    public long getSendId() {
        return sendId;
    }

    public void setSendId(long sendId) {
        this.sendId = sendId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getSendNickName() {
        return sendNickName;
    }

    public void setSendNickName(String sendNickName) {
        this.sendNickName = sendNickName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public boolean isSelfSend() {
        return selfSend;
    }

    public void setSelfSend(boolean selfSend) {
        this.selfSend = selfSend;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getReadedCount() {
        return readedCount;
    }

    public void setReadedCount(int readedCount) {
        this.readedCount = readedCount;
    }

    public String getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(String loadStatus) {
        this.loadStatus = loadStatus;
    }

    public String getAtUserIds() {
        return atUserIds;
    }

    public void setAtUserIds(String atUserIds) {
        this.atUserIds = atUserIds;
    }

    public boolean isReceipt() {
        return receipt;
    }

    public void setReceipt(boolean receipt) {
        this.receipt = receipt;
    }

    public boolean isReceiptOk() {
        return receiptOk;
    }

    public void setReceiptOk(boolean receiptOk) {
        this.receiptOk = receiptOk;
    }
    // Getters and setters
    // ...  
}