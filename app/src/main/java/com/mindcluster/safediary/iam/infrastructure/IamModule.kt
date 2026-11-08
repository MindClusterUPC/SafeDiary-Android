package com.mindcluster.safediary.iam.infrastructure

import android.content.Context
import com.mindcluster.safediary.assistantai.infrastructure.local.SafeDiaryDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mindcluster.safediary.iam.application.commands.SignInHandler
import com.mindcluster.safediary.iam.application.commands.SignOutHandler
import com.mindcluster.safediary.iam.application.commands.SignUpHandler
import com.mindcluster.safediary.iam.domain.repository.AuthRepository
import com.mindcluster.safediary.iam.infrastructure.remote.RetrofitIamGateway
import com.mindcluster.safediary.profiles.infrastructure.local.ProfilePreferences
import com.mindcluster.safediary.shared.infrastructure.network.NetworkMonitor
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionPreferences

class IamModule(context: Context) {
    private val appContext: Context = context.applicationContext
    val authSessionPreferences: AuthSessionPreferences by lazy { AuthSessionPreferences(context) }
    val profilePreferences: ProfilePreferences by lazy { ProfilePreferences(context) }
    val networkMonitor: NetworkMonitor by lazy { NetworkMonitor.getInstance(context) }
    val remoteGateway: RetrofitIamGateway by lazy { RetrofitIamGateway() }

    val authRepository: AuthRepository by lazy {
        OfflineFirstAuthRepository(
            authSessionPreferences,
            profilePreferences,
            remoteGateway,
            networkMonitor,
            clearLocalData = {
                // Chats and diary drafts of another account must never be shown to the new one.
                withContext(Dispatchers.IO) { SafeDiaryDatabase.getInstance(appContext).clearAllTables() }
            }
        )
    }

    val signInHandler: SignInHandler by lazy {
        SignInHandler(authRepository)
    }

    val signUpHandler: SignUpHandler by lazy {
        SignUpHandler(authRepository)
    }

    val signOutHandler: SignOutHandler by lazy {
        SignOutHandler(authRepository)
    }

    companion object {
        @Volatile
        private var instance: IamModule? = null

        fun getInstance(context: Context): IamModule {
            return instance ?: synchronized(this) {
                instance ?: IamModule(context.applicationContext).also { instance = it }
            }
        }
    }
}
