package com.mindcluster.safediary.profiles.infrastructure

import android.content.Context
import com.mindcluster.safediary.profiles.application.commands.*
import com.mindcluster.safediary.profiles.application.queries.*
import com.mindcluster.safediary.profiles.domain.repository.PatientProfileRepository
import com.mindcluster.safediary.profiles.infrastructure.local.ProfilePreferences
import com.mindcluster.safediary.profiles.infrastructure.remote.RetrofitPatientProfileGateway
import com.mindcluster.safediary.shared.domain.events.DomainEventPublisher
import com.mindcluster.safediary.shared.infrastructure.eventbus.InMemoryDomainEventBus
import com.mindcluster.safediary.shared.infrastructure.network.NetworkMonitor
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionPreferences

class ProfilesModule(context: Context) {
    val authSessionPreferences: AuthSessionPreferences by lazy { AuthSessionPreferences(context) }
    val profilePreferences: ProfilePreferences by lazy { ProfilePreferences(context) }
    val networkMonitor: NetworkMonitor by lazy { NetworkMonitor.getInstance(context) }
    val eventBus: DomainEventPublisher by lazy { InMemoryDomainEventBus() }
    val remoteGateway: RetrofitPatientProfileGateway by lazy { RetrofitPatientProfileGateway() }

    val repository: PatientProfileRepository by lazy {
        OfflineFirstPatientProfileRepository(
            profilePreferences,
            remoteGateway,
            networkMonitor,
            eventBus
        )
    }

    val getPatientProfileHandler: GetPatientProfileHandler by lazy {
        GetPatientProfileHandler(repository)
    }

    val updateEmergencyContactHandler: UpdateEmergencyContactHandler by lazy {
        UpdateEmergencyContactHandler(repository)
    }

    val removeEmergencyContactHandler: RemoveEmergencyContactHandler by lazy {
        RemoveEmergencyContactHandler(repository)
    }

    val updatePatientEmailHandler: UpdatePatientEmailHandler by lazy {
        UpdatePatientEmailHandler(repository)
    }

    val updateAvatarHandler: UpdateAvatarHandler by lazy {
        UpdateAvatarHandler(repository)
    }

    val updateTherapeuticTopicsHandler: UpdateTherapeuticTopicsHandler by lazy {
        UpdateTherapeuticTopicsHandler(repository)
    }

    val updatePrivacySettingsHandler: UpdatePrivacySettingsHandler by lazy {
        UpdatePrivacySettingsHandler(repository)
    }

    val updatePatientNameHandler: UpdatePatientNameHandler by lazy {
        UpdatePatientNameHandler(repository)
    }

    companion object {
        @Volatile
        private var instance: ProfilesModule? = null

        fun getInstance(context: Context): ProfilesModule {
            return instance ?: synchronized(this) {
                instance ?: ProfilesModule(context.applicationContext).also { instance = it }
            }
        }
    }
}
