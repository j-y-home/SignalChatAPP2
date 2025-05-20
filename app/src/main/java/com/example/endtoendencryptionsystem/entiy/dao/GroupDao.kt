package com.example.endtoendencryptionsystem.entiy.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.endtoendencryptionsystem.entiy.database.Group
import com.example.endtoendencryptionsystem.entiy.database.PrivateChatMessage


@Dao
interface GroupDao {

    /**
     * 添加群聊
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addGroup(group: Group)

    /**
     * 添加群聊
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addGroups(group: List<Group>)

    @Query("select * from im_group")
    fun selectAllData():List<Group>

    @Query("DELETE FROM im_group WHERE id = :id")
    fun deleteGroupById(id: Long)

    @Query("DELETE FROM im_group")
    fun deleteGroup()

    @Update
    fun updateGroup(group: Group)

}