package com.example.endtoendencryptionsystem.entiy.dao;

import androidx.room.Dao;
import androidx.room.Insert;  
import androidx.room.OnConflictStrategy;  
import androidx.room.Query;  
import androidx.room.Update;


import com.example.endtoendencryptionsystem.entiy.database.PrivateChatMessage;

import java.util.List;
  
@Dao  
public interface PrivateChatMessageDao {  
    @Query("SELECT * FROM private_chat_message WHERE conversationId = :conversationId ORDER BY sendTime ASC LIMIT :limit OFFSET :offset")
    List<PrivateChatMessage> getMessagesForConversation(long conversationId, int offset, int limit);


    @Query("SELECT * FROM private_chat_message WHERE messageId = :id")
    PrivateChatMessage getMessagesById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)  
    long insertMessage(PrivateChatMessage message);  
      
    @Update  
    void updateMessage(PrivateChatMessage message);  
      
    @Query("DELETE FROM private_chat_message WHERE conversationId = :conversationId")  
    void deleteMessagesForConversation(long conversationId);

    @Query("DELETE FROM private_chat_message WHERE messageId = :messageId")
    void deleteMessageById(String messageId);

    /**
     * 清空表
     */
    @Query("DELETE FROM private_chat_message")
    void deleteAllChats();
}