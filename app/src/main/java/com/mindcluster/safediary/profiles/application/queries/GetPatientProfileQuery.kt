package com.mindcluster.safediary.profiles.application.queries

import com.mindcluster.safediary.profiles.domain.model.PatientProfileAggregate
import com.mindcluster.safediary.profiles.domain.repository.PatientProfileRepository
import kotlinx.coroutines.flow.Flow

data class GetPatientProfileQuery(val patientId: Long)

class GetPatientProfileHandler(
    private val repository: PatientProfileRepository
) {
    suspend fun handle(query: GetPatientProfileQuery): Result<PatientProfileAggregate> = runCatching {
        repository.getProfile(query.patientId)
    }

    fun observe(patientId: Long): Flow<PatientProfileAggregate?> =
        repository.observeProfile(patientId)
}
