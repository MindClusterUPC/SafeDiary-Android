package com.mindcluster.safediary.diary.infrastructure

import android.content.Context
import com.mindcluster.safediary.assistantai.infrastructure.local.SafeDiaryDatabase
import com.mindcluster.safediary.diary.domain.repository.DiaryEntryRepository
import com.mindcluster.safediary.diary.infrastructure.remote.RetrofitDiaryGateway
import com.mindcluster.safediary.diary.infrastructure.repositories.OfflineFirstDiaryRepository

class DiaryModule private constructor(context: Context) {

    val diaryRepository: DiaryEntryRepository by lazy {
        val db = SafeDiaryDatabase.getInstance(context)
        val remoteGateway = RetrofitDiaryGateway()
        OfflineFirstDiaryRepository(db.diaryEntryDao(), remoteGateway)
    }

    companion object {
        @Volatile
        private var INSTANCE: DiaryModule? = null

        fun getInstance(context: Context): DiaryModule {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DiaryModule(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
