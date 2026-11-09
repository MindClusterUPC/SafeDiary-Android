package com.mindcluster.safediary.diary.infrastructure

import android.content.Context
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionPreferences
import com.mindcluster.safediary.shared.infrastructure.network.RetrofitClientProvider
import com.mindcluster.safediary.diary.infrastructure.remote.api.DiaryApiService
import com.mindcluster.safediary.assistantai.infrastructure.local.SafeDiaryDatabase
import com.mindcluster.safediary.diary.domain.repository.DiaryEntryRepository
import com.mindcluster.safediary.diary.infrastructure.remote.RetrofitDiaryGateway
import com.mindcluster.safediary.diary.infrastructure.repositories.OfflineFirstDiaryRepository

class DiaryModule private constructor(context: Context) {
    val accountId = AuthSessionPreferences(context).patientAccountId
    val sessionToken = AuthSessionPreferences(context).authToken

    val diaryRepository: DiaryEntryRepository by lazy {
        val db = SafeDiaryDatabase.getInstance(context, accountId)
        val remoteGateway = RetrofitDiaryGateway(RetrofitClientProvider.createSessionService<DiaryApiService>(sessionToken))
        OfflineFirstDiaryRepository(db.diaryEntryDao(), remoteGateway)
    }

    companion object {
        @Volatile
        private var INSTANCE: DiaryModule? = null

        fun getInstance(context: Context): DiaryModule {
            return synchronized(this) {
                val session = AuthSessionPreferences(context)
                INSTANCE?.takeIf { it.accountId == session.patientAccountId && it.sessionToken == session.authToken }
                    ?: DiaryModule(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
