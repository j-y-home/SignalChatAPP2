package com.example.endtoendencryptionsystem.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.endtoendencryptionsystem.entiy.dao.ChatConversationDao
import com.example.endtoendencryptionsystem.entiy.dao.FriendsDao
import com.example.endtoendencryptionsystem.entiy.dao.GroupChatMessageDao
import com.example.endtoendencryptionsystem.entiy.dao.GroupDao
import com.example.endtoendencryptionsystem.entiy.dao.MetadataDao
import com.example.endtoendencryptionsystem.entiy.dao.PrivateChatMessageDao
import com.example.endtoendencryptionsystem.entiy.dao.PrivateMessageDao
import com.example.endtoendencryptionsystem.entiy.database.ChatConversation
import com.example.endtoendencryptionsystem.entiy.database.ChatMetadata
import com.example.endtoendencryptionsystem.entiy.database.Friend
import com.example.endtoendencryptionsystem.entiy.database.Group
import com.example.endtoendencryptionsystem.entiy.database.GroupChatMessage
import com.example.endtoendencryptionsystem.entiy.database.GroupMessage
import com.example.endtoendencryptionsystem.entiy.database.PrivateChatMessage
import com.example.endtoendencryptionsystem.entiy.database.PrivateMessage
import com.example.endtoendencryptionsystem.utils.Converters


@Database(
    entities = [Friend::class, PrivateMessage::class, GroupMessage::class,
        ChatMetadata::class, ChatConversation::class,
        PrivateChatMessage::class, GroupChatMessage::class, Group::class], version = 2, exportSchema = false)
@TypeConverters(value = [Converters::class])
abstract class AppDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendsDao
    abstract fun privateMessageDao(): PrivateMessageDao
    abstract fun metadataDao(): MetadataDao
    abstract fun chatConversationDao(): ChatConversationDao
    abstract fun privateChatMessageDao(): PrivateChatMessageDao
    abstract fun groupChatMessageDao(): GroupChatMessageDao
    abstract fun groupDao(): GroupDao

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