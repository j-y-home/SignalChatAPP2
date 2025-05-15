package com.example.endtoendencryptionsystem.entiy.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.endtoendencryptionsystem.entiy.database.ChatConversation;

import java.util.List;

@Dao
public interface ChatConversationDao {  
    @Query("SELECT * FROM chat_conversation WHERE userId = :userId AND deleted = 0 ORDER BY lastSendTime DESC")
    List<ChatConversation> getAllConversations(long userId);
      
    @Query("SELECT * FROM chat_conversation WHERE userId = :userId AND targetId = :targetId AND type = :type")  
    ChatConversation getConversation(long userId, long targetId, String type);  
      
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertConversation(ChatConversation conversation);  
      
    @Update
    void updateConversation(ChatConversation conversation);  
      
    @Query("UPDATE chat_conversation SET deleted = 1 WHERE userId = :userId AND targetId = :targetId AND type = :type")  
    void markConversationAsDeleted(long userId, long targetId, String type);

    /**
     * 清空表
     */
    @Query("DELETE FROM chat_conversation")
    void deleteAll();

    @Query("DELETE FROM chat_conversation WHERE userId = :userId AND targetId = :targetId AND type = :type")
    void deleteConversation(long userId, long targetId, String type);

    @Query("DELETE FROM chat_conversation WHERE id = :id")
    void deleteConversationById(long id);
}