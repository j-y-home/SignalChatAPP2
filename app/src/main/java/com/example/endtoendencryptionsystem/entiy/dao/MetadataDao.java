package com.example.endtoendencryptionsystem.entiy.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;  
import androidx.room.Update;

import com.example.endtoendencryptionsystem.entiy.database.ChatMetadata;


@Dao
public interface MetadataDao {  
    @Query("SELECT * FROM chat_metadata WHERE userId = :userId")
    ChatMetadata getMetadata(long userId);
      
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMetadata(ChatMetadata metadata);  
      
    @Update  
    void updateMetadata(ChatMetadata metadata);

    /**
     * 清空表
     */
    @Query("DELETE FROM chat_metadata")
    void deleteAll();
}