package com.mindcluster.safediary.profiles.application.commands

import com.mindcluster.safediary.profiles.domain.model.PatientProfileAggregate
import com.mindcluster.safediary.profiles.domain.repository.PatientProfileRepository

data class UpdatePatientEmailCommand(
    val patientId: Long,
    val newEmail: String,
    val currentPassword: String
)

class UpdatePatientEmailHandler(
    private val repository: PatientProfileRepository
) {
    suspend fun handle(command: UpdatePatientEmailCommand): Result<PatientProfileAggregate> = runCatching {
        repository.updateEmail(command.patientId, command.newEmail, command.currentPassword)
    }
}
