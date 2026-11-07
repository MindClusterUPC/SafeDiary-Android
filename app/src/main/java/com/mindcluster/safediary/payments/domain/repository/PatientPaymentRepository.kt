package com.mindcluster.safediary.payments.domain.repository

import com.mindcluster.safediary.payments.domain.model.PatientSubscriptionAggregate
import com.mindcluster.safediary.payments.domain.model.PaymentHistoryItem
import com.mindcluster.safediary.payments.domain.model.SubscriptionPlan
import kotlinx.coroutines.flow.Flow

interface PatientPaymentRepository {
    fun observeSubscription(patientAccountId: Long): Flow<PatientSubscriptionAggregate?>
    suspend fun getSubscription(patientAccountId: Long): PatientSubscriptionAggregate
    suspend fun createSubscriptionCheckout(
        patientAccountId: Long,
        plan: SubscriptionPlan
    ): String? // returns checkoutUrl if required, or null if FREE activated
    suspend fun cancelSubscription(patientAccountId: Long): PatientSubscriptionAggregate
    suspend fun updatePreferences(
        patientAccountId: Long,
        autoDebit: Boolean,
        requireFaceId: Boolean
    ): PatientSubscriptionAggregate
    suspend fun getPaymentHistory(patientAccountId: Long): List<PaymentHistoryItem>
    suspend fun setDefaultPaymentMethod(patientAccountId: Long, cardId: String): PatientSubscriptionAggregate
}
