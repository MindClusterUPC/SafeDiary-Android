package com.mindcluster.safediary.iam.infrastructure

import android.content.Context
import com.mindcluster.safediary.iam.application.commands.SignInHandler
import com.mindcluster.safediary.iam.application.commands.SignOutHandler
import com.mindcluster.safediary.iam.domain.repository.AuthRepository
import com.mindcluster.safediary.iam.infrastructure.remote.RetrofitIamGateway
import com.mindcluster.safediary.profiles.infrastructure.local.ProfilePreferences
import com.mindcluster.safediary.shared.infrastructure.network.NetworkMonitor
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionPreferences

class IamModule(context: Context) {
    val authSessionPreferences: AuthSessionPreferences by lazy { AuthSessionPreferences(context) }
    val profilePreferences: ProfilePreferences by lazy { ProfilePreferences(context) }
    val networkMonitor: NetworkMonitor by lazy { NetworkMonitor.getInstance(context) }
    val remoteGateway: RetrofitIamGateway by lazy { RetrofitIamGateway() }

    val authRepository: AuthRepository by lazy {
        OfflineFirstAuthRepository(
            authSessionPreferences,
            profilePreferences,
            remoteGateway,
            networkMonitor
        )
    }

    val signInHandler: SignInHandler by lazy {
        SignInHandler(authRepository)
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
