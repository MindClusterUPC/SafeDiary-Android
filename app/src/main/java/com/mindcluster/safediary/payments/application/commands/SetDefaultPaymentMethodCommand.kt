package com.mindcluster.safediary.payments.application.commands

import com.mindcluster.safediary.payments.domain.model.PatientSubscriptionAggregate
import com.mindcluster.safediary.payments.domain.repository.PatientPaymentRepository

data class SetDefaultPaymentMethodCommand(
    val patientAccountId: Long,
    val cardId: String
)

class SetDefaultPaymentMethodHandler(
    private val repository: PatientPaymentRepository
) {
    suspend fun handle(command: SetDefaultPaymentMethodCommand): Result<PatientSubscriptionAggregate> = runCatching {
        repository.setDefaultPaymentMethod(command.patientAccountId, command.cardId)
    }
}
