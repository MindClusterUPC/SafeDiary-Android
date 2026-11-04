package com.mindcluster.safediary.profiles.application.commands

import com.mindcluster.safediary.profiles.domain.model.PatientProfileAggregate
import com.mindcluster.safediary.profiles.domain.model.TherapeuticTopic
import com.mindcluster.safediary.profiles.domain.repository.PatientProfileRepository

data class UpdateTherapeuticTopicsCommand(
    val patientId: Long,
    val topics: Set<TherapeuticTopic>
)

class UpdateTherapeuticTopicsHandler(
    private val repository: PatientProfileRepository
) {
    suspend fun handle(command: UpdateTherapeuticTopicsCommand): Result<PatientProfileAggregate> = runCatching {
        repository.updateTherapeuticTopics(command.patientId, command.topics)
    }
}
