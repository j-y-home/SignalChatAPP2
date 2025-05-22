package com.example.endtoendencryptionsystem.entiy.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.endtoendencryptionsystem.entiy.database.SenderKeyEntity
import com.example.endtoendencryptionsystem.entiy.database.SignalSessionEntity

@Dao
interface SignalSessionDao {
    /**
     * Get a session by address and device ID
     */
    @Query("SELECT * FROM signal_sessions WHERE address = :address AND deviceId = :deviceId")
    fun getSession(address: String, deviceId: Int): SignalSessionEntity?

    /**
     * Insert or update a session
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(session: SignalSessionEntity): Long

    /**
     * Delete a session
     */
    @Query("DELETE FROM signal_sessions WHERE address = :address AND deviceId = :deviceId")
    fun deleteSession(address: String, deviceId: Int)

    /**
     * Delete all sessions for an address
     */
    @Query("DELETE FROM signal_sessions WHERE address = :address")
    fun deleteAllSessions(address: String)

    /**
     * Check if a session exists
     */
    @Query("SELECT COUNT(*) FROM signal_sessions WHERE address = :address AND deviceId = :deviceId")
    fun containsSession(address: String, deviceId: Int): Int
}