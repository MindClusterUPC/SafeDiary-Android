package com.mindcluster.safediary.profiles.domain.events

import com.mindcluster.safediary.shared.domain.events.DomainEvent
import java.time.Instant

data class PatientProfileUpdatedEvent(
    val patientId: Long,
    override val occurredOn: Instant = Instant.now()
) : DomainEvent
