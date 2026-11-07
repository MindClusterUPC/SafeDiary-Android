package com.mindcluster.safediary.profiles.application.commands

import com.mindcluster.safediary.profiles.domain.model.PatientProfileAggregate
import com.mindcluster.safediary.profiles.domain.repository.PatientProfileRepository

data class RemoveEmergencyContactCommand(
    val patientId: Long
)

class RemoveEmergencyContactHandler(
    private val repository: PatientProfileRepository
) {
    suspend fun handle(command: RemoveEmergencyContactCommand): Result<PatientProfileAggregate> = runCatching {
        repository.removeEmergencyContact(command.patientId)
    }
}
