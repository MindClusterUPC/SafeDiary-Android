package com.mindcluster.safediary.profiles.application.commands

import com.mindcluster.safediary.profiles.domain.model.EmergencyContact
import com.mindcluster.safediary.profiles.domain.model.PatientProfileAggregate
import com.mindcluster.safediary.profiles.domain.repository.PatientProfileRepository

data class UpdateEmergencyContactCommand(
    val patientId: Long,
    val contact: EmergencyContact
)

class UpdateEmergencyContactHandler(
    private val repository: PatientProfileRepository
) {
    suspend fun handle(command: UpdateEmergencyContactCommand): Result<PatientProfileAggregate> = runCatching {
        repository.updateEmergencyContact(command.patientId, command.contact)
    }
}
