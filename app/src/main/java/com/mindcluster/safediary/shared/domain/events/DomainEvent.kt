package com.mindcluster.safediary.shared.domain.events

import java.time.Instant

interface DomainEvent {
    val occurredOn: Instant
}
