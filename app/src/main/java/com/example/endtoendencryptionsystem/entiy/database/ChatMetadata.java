package com.example.endtoendencryptionsystem.entiy.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 元数据
 */
@Entity(tableName = "chat_metadata")  
public class ChatMetadata {  
    @PrimaryKey  
    private long userId;  
    private long privateMsgMaxId;  
    private long groupMsgMaxId;  
    private long lastUpdateTime;  
  
    // Getters and setters  
    public long getUserId() { return userId; }  
    public void setUserId(long userId) { this.userId = userId; }  
      
    public long getPrivateMsgMaxId() { return privateMsgMaxId; }  
    public void setPrivateMsgMaxId(long privateMsgMaxId) { this.privateMsgMaxId = privateMsgMaxId; }  
      
    public long getGroupMsgMaxId() { return groupMsgMaxId; }  
    public void setGroupMsgMaxId(long groupMsgMaxId) { this.groupMsgMaxId = groupMsgMaxId; }  
      
    public long getLastUpdateTime() { return lastUpdateTime; }  
    public void setLastUpdateTime(long lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }  
}