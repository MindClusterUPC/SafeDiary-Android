package com.mindcluster.safediary.payments.application.queries

import com.mindcluster.safediary.payments.domain.model.PatientSubscriptionAggregate
import com.mindcluster.safediary.payments.domain.repository.PatientPaymentRepository
import kotlinx.coroutines.flow.Flow

data class GetCurrentSubscriptionQuery(val patientAccountId: Long)

class GetCurrentSubscriptionHandler(
    private val repository: PatientPaymentRepository
) {
    suspend fun handle(query: GetCurrentSubscriptionQuery): Result<PatientSubscriptionAggregate> = runCatching {
        repository.getSubscription(query.patientAccountId)
    }

    fun observe(patientAccountId: Long): Flow<PatientSubscriptionAggregate?> =
        repository.observeSubscription(patientAccountId)
}
