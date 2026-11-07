package com.mindcluster.safediary.payments.application.queries

import com.mindcluster.safediary.payments.domain.model.PaymentHistoryItem
import com.mindcluster.safediary.payments.domain.repository.PatientPaymentRepository

data class GetPaymentHistoryQuery(val patientAccountId: Long)

class GetPaymentHistoryHandler(
    private val repository: PatientPaymentRepository
) {
    suspend fun handle(query: GetPaymentHistoryQuery): Result<List<PaymentHistoryItem>> = runCatching {
        repository.getPaymentHistory(query.patientAccountId)
    }
}
