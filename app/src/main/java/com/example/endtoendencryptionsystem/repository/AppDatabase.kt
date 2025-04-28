package com.example.endtoendencryptionsystem.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.endtoendencryptionsystem.entiy.dao.FriendsDao
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.GroupMessage
import com.example.endtoendencryptionsystem.entiy.database.PrivateMessage
import com.example.endtoendencryptionsystem.utils.Converters


@Database(
    entities = [Friend::class], version = 1, exportSchema = false)
@TypeConverters(value = [Converters::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendsDao


    companion object {
        @Volatile
        private var DB: AppDatabase? = null
        private const val DBName = "chat_signal.db"

        fun getDatabase(context: Context): AppDatabase {
            return DB ?: synchronized(this) {
                val db = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DBName)
                    .build()
                DB = db
                db
            }
        }
    }
}