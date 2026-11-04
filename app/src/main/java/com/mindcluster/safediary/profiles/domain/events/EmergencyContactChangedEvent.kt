package com.mindcluster.safediary.profiles.domain.events

import com.mindcluster.safediary.shared.domain.events.DomainEvent
import java.time.Instant

data class EmergencyContactChangedEvent(
    val patientId: Long,
    val isRemoved: Boolean,
    override val occurredOn: Instant = Instant.now()
) : DomainEvent
