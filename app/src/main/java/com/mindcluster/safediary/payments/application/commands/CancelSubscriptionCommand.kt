package com.mindcluster.safediary.payments.application.commands

import com.mindcluster.safediary.payments.domain.model.PatientSubscriptionAggregate
import com.mindcluster.safediary.payments.domain.repository.PatientPaymentRepository

data class CancelSubscriptionCommand(
    val patientAccountId: Long
)

class CancelSubscriptionHandler(
    private val repository: PatientPaymentRepository
) {
    suspend fun handle(command: CancelSubscriptionCommand): Result<PatientSubscriptionAggregate> = runCatching {
        repository.cancelSubscription(command.patientAccountId)
    }
}
