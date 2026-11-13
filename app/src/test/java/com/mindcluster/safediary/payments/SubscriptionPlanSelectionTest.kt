package com.mindcluster.safediary.payments

import com.mindcluster.safediary.payments.application.commands.CancelSubscriptionCommand
import com.mindcluster.safediary.payments.application.commands.CancelSubscriptionHandler
import com.mindcluster.safediary.payments.application.commands.CreateSubscriptionCheckoutCommand
import com.mindcluster.safediary.payments.application.commands.CreateSubscriptionCheckoutHandler
import com.mindcluster.safediary.payments.domain.model.PatientSubscriptionAggregate
import com.mindcluster.safediary.payments.domain.model.PaymentHistoryItem
import com.mindcluster.safediary.payments.domain.model.SubscriptionPlan
import com.mindcluster.safediary.payments.domain.model.SubscriptionStatus
import com.mindcluster.safediary.payments.domain.repository.PatientPaymentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionPlanSelectionTest {

    private class FakeSubscriptionRepository(
        private val checkoutUrl: Result<String?>
    ) : PatientPaymentRepository {
        var selectedPlan: SubscriptionPlan? = null
        var cancelledAccountId: Long? = null
        private val flow = MutableStateFlow<PatientSubscriptionAggregate?>(null)

        override fun observeSubscription(patientAccountId: Long): Flow<PatientSubscriptionAggregate?> = flow
        override suspend fun getSubscription(patientAccountId: Long) = PatientSubscriptionAggregate(patientAccountId)
        override suspend fun createSubscriptionCheckout(patientAccountId: Long, plan: SubscriptionPlan): String? {
            selectedPlan = plan
            return checkoutUrl.getOrThrow()
        }
        override suspend fun createPaymentMethodSetup(patientAccountId: Long): String = ""
        override suspend fun cancelSubscription(patientAccountId: Long): PatientSubscriptionAggregate {
            cancelledAccountId = patientAccountId
            return PatientSubscriptionAggregate(patientAccountId, SubscriptionPlan.FREE, SubscriptionStatus.CANCELLED)
        }
        override suspend fun updatePreferences(patientAccountId: Long, autoDebit: Boolean, requireFaceId: Boolean) =
            PatientSubscriptionAggregate(patientAccountId)
        override suspend fun getPaymentHistory(patientAccountId: Long): List<PaymentHistoryItem> = emptyList()
        override suspend fun setDefaultPaymentMethod(patientAccountId: Long, cardId: String) =
            PatientSubscriptionAggregate(patientAccountId)
    }

    @Test
    fun selectingPaidPlanReturnsStripeCheckoutUrl() = runTest {
        val repo = FakeSubscriptionRepository(Result.success("https://checkout.stripe.com/c/pay/cs_test_terra"))
        val handler = CreateSubscriptionCheckoutHandler(repo)

        val result = handler.handle(CreateSubscriptionCheckoutCommand(5L, SubscriptionPlan.TERRA))

        assertTrue(result.isSuccess)
        assertEquals(SubscriptionPlan.TERRA, repo.selectedPlan)
        assertEquals("https://checkout.stripe.com/c/pay/cs_test_terra", result.getOrNull())
    }

    @Test
    fun cancellingPaidPlanDowngradesToFree() = runTest {
        val repo = FakeSubscriptionRepository(Result.success(null))
        val handler = CancelSubscriptionHandler(repo)

        val result = handler.handle(CancelSubscriptionCommand(5L))

        assertTrue(result.isSuccess)
        assertEquals(5L, repo.cancelledAccountId)
        assertEquals(SubscriptionPlan.FREE, result.getOrNull()?.plan)
    }
}
