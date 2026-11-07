package com.mindcluster.safediary.assistantai.infrastructure.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mindcluster.safediary.diary.infrastructure.local.dao.DiaryEntryDao
import com.mindcluster.safediary.diary.infrastructure.local.entity.DiaryEntryEntity

@Database(
    entities = [ConversationEntity::class, MessageEntity::class, DiaryEntryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SafeDiaryDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun diaryEntryDao(): DiaryEntryDao

    companion object {
        @Volatile
        private var INSTANCE: SafeDiaryDatabase? = null

        fun getInstance(context: Context): SafeDiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SafeDiaryDatabase::class.java,
                    "safediary.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
