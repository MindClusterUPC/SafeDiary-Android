package com.mindcluster.safediary.payments.application.commands

import com.mindcluster.safediary.payments.domain.repository.PatientPaymentRepository

data class CreatePaymentMethodSetupCommand(
    val patientAccountId: Long
)

class CreatePaymentMethodSetupHandler(
    private val repository: PatientPaymentRepository
) {
    suspend fun handle(command: CreatePaymentMethodSetupCommand): Result<String> = runCatching {
        repository.createPaymentMethodSetup(command.patientAccountId)
    }
}
