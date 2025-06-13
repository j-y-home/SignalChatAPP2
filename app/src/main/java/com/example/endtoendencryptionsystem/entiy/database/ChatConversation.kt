package com.example.endtoendencryptionsystem.entiy.database;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity(
    tableName = "chat_conversation",  
    indices = {  
        @Index(value = {"userId", "targetId", "type"}, unique = true),
        @Index("userId"),  
        @Index("targetId"),  
        @Index("lastSendTime")  
    }  
)  
public class ChatConversation {  
    @PrimaryKey(autoGenerate = true)
    private long id;  
    private long userId;  
    private long targetId;  
    private String type;  
    private String showName;  
    private String headImage;  
    private String lastContent;  
    private long lastSendTime;  
    private int unreadCount;  
    private boolean atMe;  
    private boolean atAll;  
    private long lastTimeTip;
    @Nullable
    private String sendNickName;
    private boolean deleted;
    private boolean stored;
    @Ignore
    private List<Object> messages = new ArrayList<>();
    // Getters and setters...

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getTargetId() {
        return targetId;
    }

    public void setTargetId(long targetId) {
        this.targetId = targetId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public String getHeadImage() {
        return headImage;
    }

    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }

    public String getLastContent() {
        return lastContent;
    }

    public void setLastContent(String lastContent) {
        this.lastContent = lastContent;
    }

    public long getLastSendTime() {
        return lastSendTime;
    }

    public void setLastSendTime(long lastSendTime) {
        this.lastSendTime = lastSendTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public boolean isAtMe() {
        return atMe;
    }

    public void setAtMe(boolean atMe) {
        this.atMe = atMe;
    }

    public boolean isAtAll() {
        return atAll;
    }

    public void setAtAll(boolean atAll) {
        this.atAll = atAll;
    }

    public long getLastTimeTip() {
        return lastTimeTip;
    }

    public void setLastTimeTip(long lastTimeTip) {
        this.lastTimeTip = lastTimeTip;
    }

    public String getSendNickName() {
        return sendNickName;
    }

    public void setSendNickName(String sendNickName) {
        this.sendNickName = sendNickName;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public List<Object> getMessages() {
        return messages;
    }

    public void setMessages(List<Object> messages) {
        this.messages = messages;
    }

    public boolean isStored() {
        return stored;
    }

    public void setStored(boolean stored) {
        this.stored = stored;
    }
}