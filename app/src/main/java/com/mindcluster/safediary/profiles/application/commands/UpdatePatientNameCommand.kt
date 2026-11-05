package com.mindcluster.safediary.profiles.application.commands

import com.mindcluster.safediary.profiles.domain.model.PatientProfileAggregate
import com.mindcluster.safediary.profiles.domain.repository.PatientProfileRepository

data class UpdatePatientNameCommand(
    val patientId: Long,
    val firstName: String,
    val lastName: String
)

class UpdatePatientNameHandler(
    private val repository: PatientProfileRepository
) {
    suspend fun handle(command: UpdatePatientNameCommand): Result<PatientProfileAggregate> = runCatching {
        repository.updateName(command.patientId, command.firstName, command.lastName)
    }
}
