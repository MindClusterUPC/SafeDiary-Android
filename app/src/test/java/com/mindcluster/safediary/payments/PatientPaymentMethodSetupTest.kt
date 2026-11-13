package com.mindcluster.safediary.payments

import com.mindcluster.safediary.payments.application.commands.CreatePaymentMethodSetupCommand
import com.mindcluster.safediary.payments.application.commands.CreatePaymentMethodSetupHandler
import com.mindcluster.safediary.payments.domain.model.PatientSubscriptionAggregate
import com.mindcluster.safediary.payments.domain.model.PaymentHistoryItem
import com.mindcluster.safediary.payments.domain.model.SubscriptionPlan
import com.mindcluster.safediary.payments.domain.repository.PatientPaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PatientPaymentMethodSetupTest {

    private class FakePaymentRepository(
        private val setupUrl: Result<String>
    ) : PatientPaymentRepository {
        var requestedPatientId: Long? = null
        private val flow = MutableStateFlow<PatientSubscriptionAggregate?>(null)

        override fun observeSubscription(patientAccountId: Long): Flow<PatientSubscriptionAggregate?> = flow
        override suspend fun getSubscription(patientAccountId: Long) = PatientSubscriptionAggregate(patientAccountId)
        override suspend fun createSubscriptionCheckout(patientAccountId: Long, plan: SubscriptionPlan): String? = null
        override suspend fun createPaymentMethodSetup(patientAccountId: Long): String {
            requestedPatientId = patientAccountId
            return setupUrl.getOrThrow()
        }
        override suspend fun cancelSubscription(patientAccountId: Long) = PatientSubscriptionAggregate(patientAccountId)
        override suspend fun updatePreferences(patientAccountId: Long, autoDebit: Boolean, requireFaceId: Boolean) =
            PatientSubscriptionAggregate(patientAccountId)
        override suspend fun getPaymentHistory(patientAccountId: Long): List<PaymentHistoryItem> = emptyList()
        override suspend fun setDefaultPaymentMethod(patientAccountId: Long, cardId: String) =
            PatientSubscriptionAggregate(patientAccountId)
    }

    @Test
    fun createPaymentMethodSetupHandlerReturnsStripeCheckoutUrl() = runTest {
        val repo = FakePaymentRepository(Result.success("https://checkout.stripe.com/c/pay/cs_test_setup_123"))
        val handler = CreatePaymentMethodSetupHandler(repo)

        val result = handler.handle(CreatePaymentMethodSetupCommand(patientAccountId = 5L))

        assertTrue(result.isSuccess)
        assertEquals(5L, repo.requestedPatientId)
        assertEquals("https://checkout.stripe.com/c/pay/cs_test_setup_123", result.getOrNull())
    }

    @Test
    fun createPaymentMethodSetupHandlerPropagatesFailures() = runTest {
        val repo = FakePaymentRepository(Result.failure(IllegalStateException("Sin conexión a internet")))
        val handler = CreatePaymentMethodSetupHandler(repo)

        val result = handler.handle(CreatePaymentMethodSetupCommand(patientAccountId = 5L))

        assertTrue(result.isFailure)
        assertEquals("Sin conexión a internet", result.exceptionOrNull()?.message)
    }
}
