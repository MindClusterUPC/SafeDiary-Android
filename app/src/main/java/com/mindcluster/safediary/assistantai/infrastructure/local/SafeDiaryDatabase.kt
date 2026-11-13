package com.mindcluster.safediary.assistantai.infrastructure.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mindcluster.safediary.shared.infrastructure.security.AccountStorage
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionPreferences
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
        private val instances = mutableMapOf<Long, SafeDiaryDatabase>()

        fun getInstance(context: Context, accountId: Long = AuthSessionPreferences(context).patientAccountId): SafeDiaryDatabase {
            return synchronized(this) {
                instances.getOrPut(accountId) { Room.databaseBuilder(
                    context.applicationContext,
                    SafeDiaryDatabase::class.java,
                    AccountStorage.databaseName(accountId)
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                }
            }
        }
    }
}
