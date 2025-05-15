package com.example.endtoendencryptionsystem.entiy.dao;

import androidx.room.Dao;
import androidx.room.Insert;  
import androidx.room.OnConflictStrategy;  
import androidx.room.Query;  
import androidx.room.Update;


import com.example.endtoendencryptionsystem.entiy.database.PrivateChatMessage;

import java.util.ArrayList;
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
     * 根据chatId删除消息
     * @param chatId
     */
    @Query("DELETE FROM private_chat_message WHERE conversationId = :chatId")
    void deleteMessagesByChatId(long chatId);

    /**
     * 如果拿不到chatId，就用这个方法
     * @param userId
     * @param targetId
     */
    @Query("DELETE FROM private_chat_message WHERE (sendId = :userId AND recvId = :targetId) OR (sendId = :targetId AND recvId = :userId)")
    void deleteMessagesByUserIdAndTargetId(long userId, long targetId);


    /**
     * 清空表
     */
    @Query("DELETE FROM private_chat_message")
    void deleteAllChats();

    /**
     * 批量更新消息已读状态
     * @param messageIds
     * @param status
     */
    @Query("UPDATE private_chat_message SET status = :status WHERE messageId IN (:messageIds)")
    void updateMessagesReadStatus(ArrayList<String> messageIds, int status);
}