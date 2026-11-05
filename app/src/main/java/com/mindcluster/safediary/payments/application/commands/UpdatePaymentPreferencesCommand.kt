package com.mindcluster.safediary.payments.application.commands

import com.mindcluster.safediary.payments.domain.model.PatientSubscriptionAggregate
import com.mindcluster.safediary.payments.domain.repository.PatientPaymentRepository

data class UpdatePaymentPreferencesCommand(
    val patientAccountId: Long,
    val autoDebit: Boolean,
    val requireFaceId: Boolean
)

class UpdatePaymentPreferencesHandler(
    private val repository: PatientPaymentRepository
) {
    suspend fun handle(command: UpdatePaymentPreferencesCommand): Result<PatientSubscriptionAggregate> = runCatching {
        repository.updatePreferences(command.patientAccountId, command.autoDebit, command.requireFaceId)
    }
}
