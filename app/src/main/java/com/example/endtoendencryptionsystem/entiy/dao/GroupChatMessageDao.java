package com.example.endtoendencryptionsystem.entiy.dao;

import androidx.room.Dao;
import androidx.room.Insert;  
import androidx.room.OnConflictStrategy;  
import androidx.room.Query;  
import androidx.room.Update;


import com.example.endtoendencryptionsystem.entiy.database.GroupChatMessage;

import java.util.List;
  
@Dao  
public interface GroupChatMessageDao {  
    @Query("SELECT * FROM group_chat_message WHERE conversationId = :conversationId ORDER BY sendTime DESC LIMIT :limit OFFSET :offset")  
    List<GroupChatMessage> getMessagesForConversation(long conversationId, int offset, int limit);
      
    @Insert(onConflict = OnConflictStrategy.REPLACE)  
    long insertMessage(GroupChatMessage message);  
      
    @Update  
    void updateMessage(GroupChatMessage message);  
      
    @Query("DELETE FROM group_chat_message WHERE conversationId = :conversationId")  
    void deleteMessagesForConversation(long conversationId);

    @Query("DELETE FROM group_chat_message WHERE messageId = :messageId")
    void deleteMessageById(String messageId);

    /**
     * 清空表
     */
    @Query("DELETE FROM group_chat_message")
    void deleteAllChats();
}