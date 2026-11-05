package com.mindcluster.safediary.payments.domain.events

import com.mindcluster.safediary.payments.domain.model.SubscriptionPlan
import com.mindcluster.safediary.shared.domain.events.DomainEvent
import java.time.Instant

data class SubscriptionPlanChangedEvent(
    val patientAccountId: Long,
    val oldPlan: SubscriptionPlan,
    val newPlan: SubscriptionPlan,
    override val occurredOn: Instant = Instant.now()
) : DomainEvent
