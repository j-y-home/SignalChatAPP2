package com.example.endtoendencryptionsystem.entiy.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.endtoendencryptionsystem.entiy.database.SenderKeyEntity

@Dao
interface SenderKeyDao {  
    @Insert(onConflict = OnConflictStrategy.REPLACE)  
    fun insertOrUpdate(senderKey: SenderKeyEntity)  
      
    @Query("SELECT * FROM sender_keys WHERE keyId = :keyId")  
    fun getById(keyId: String): SenderKeyEntity?  
      
    @Query("DELETE FROM sender_keys WHERE keyId LIKE :groupId || '%'")  
    fun deleteByGroupId(groupId: String)  
}