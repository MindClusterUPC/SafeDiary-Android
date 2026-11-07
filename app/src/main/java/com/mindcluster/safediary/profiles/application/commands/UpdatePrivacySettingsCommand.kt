package com.mindcluster.safediary.profiles.application.commands

import com.mindcluster.safediary.profiles.domain.model.PatientProfileAggregate
import com.mindcluster.safediary.profiles.domain.repository.PatientProfileRepository

data class UpdatePrivacySettingsCommand(
    val patientId: Long,
    val biometricLock: Boolean,
    val dailyReminder: Boolean
)

class UpdatePrivacySettingsHandler(
    private val repository: PatientProfileRepository
) {
    suspend fun handle(command: UpdatePrivacySettingsCommand): Result<PatientProfileAggregate> = runCatching {
        repository.updatePrivacySettings(command.patientId, command.biometricLock, command.dailyReminder)
    }
}
