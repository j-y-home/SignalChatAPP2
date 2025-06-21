package com.example.endtoendencryptionsystem.entiy.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity(
    tableName = "private_chat_message",
    foreignKeys = @ForeignKey(
        entity = ChatConversation.class,
        parentColumns = "id",
        childColumns = "conversationId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index("conversationId"),
        @Index("sendTime")
    }
)
public class PrivateChatMessage implements MultiItemEntity {
    @PrimaryKey
    @NonNull
    private String messageId;
    @Nullable
    private String tmpId;
    @Nullable
    private long conversationId;
    @Nullable
    private long sendId;
    @Nullable
    private long recvId;
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
    private String loadStatus;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
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

    public long getRecvId() {
        return recvId;
    }

    public void setRecvId(long recvId) {
        this.recvId = recvId;
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

    public String getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(String loadStatus) {
        this.loadStatus = loadStatus;
    }

    @Override
    public int getItemType() {
         if(selfSend){
             return 1;
         }else{
             return 0;
         }
    }
}