package com.mindcluster.safediary.payments.domain.model

import com.mindcluster.safediary.payments.domain.events.SubscriptionPlanChangedEvent
import com.mindcluster.safediary.shared.domain.model.AggregateRoot

class PatientSubscriptionAggregate(
    val patientAccountId: Long,
    var plan: SubscriptionPlan = SubscriptionPlan.FREE,
    var status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    var activeUntil: String? = null,
    var active: Boolean = true,
    var autoDebitEnabled: Boolean = true,
    var requireFaceIdForCharges: Boolean = true,
    var paymentMethods: List<PaymentMethodCard> = emptyList()
) : AggregateRoot() {

    fun updatePlan(newPlan: SubscriptionPlan, newStatus: SubscriptionStatus, newActiveUntil: String?) {
        val old = plan
        plan = newPlan
        status = newStatus
        activeUntil = newActiveUntil
        active = newStatus == SubscriptionStatus.ACTIVE
        if (old != newPlan) {
            raise(SubscriptionPlanChangedEvent(patientAccountId, old, newPlan))
        }
    }

    fun cancelSubscription() {
        val old = plan
        plan = SubscriptionPlan.FREE
        status = SubscriptionStatus.CANCELLED
        activeUntil = null
        active = false
        raise(SubscriptionPlanChangedEvent(patientAccountId, old, SubscriptionPlan.FREE))
    }

    fun updatePreferences(autoDebit: Boolean, requireFaceId: Boolean) {
        autoDebitEnabled = autoDebit
        requireFaceIdForCharges = requireFaceId
    }

    fun setDefaultCard(cardId: String) {
        paymentMethods = paymentMethods.map {
            it.copy(isDefault = it.id == cardId)
        }
    }
}
