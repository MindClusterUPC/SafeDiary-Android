package com.mindcluster.safediary.shared.domain.events

import kotlinx.coroutines.flow.SharedFlow

interface DomainEventPublisher {
    suspend fun publish(event: DomainEvent)
    suspend fun publish(events: Collection<DomainEvent>)
    fun events(): SharedFlow<DomainEvent>
}
