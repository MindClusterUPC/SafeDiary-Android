package com.mindcluster.safediary.payments.application.commands

import com.mindcluster.safediary.payments.domain.model.SubscriptionPlan
import com.mindcluster.safediary.payments.domain.repository.PatientPaymentRepository

data class CreateSubscriptionCheckoutCommand(
    val patientAccountId: Long,
    val plan: SubscriptionPlan
)

class CreateSubscriptionCheckoutHandler(
    private val repository: PatientPaymentRepository
) {
    suspend fun handle(command: CreateSubscriptionCheckoutCommand): Result<String?> = runCatching {
        repository.createSubscriptionCheckout(command.patientAccountId, command.plan)
    }
}
