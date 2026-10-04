package com.mindcluster.safediary.shared.infrastructure.eventbus

import com.mindcluster.safediary.shared.domain.events.DomainEvent
import com.mindcluster.safediary.shared.domain.events.DomainEventPublisher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class InMemoryDomainEventBus : DomainEventPublisher {
    private val _events = MutableSharedFlow<DomainEvent>(extraBufferCapacity = 64)

    override suspend fun publish(event: DomainEvent) {
        _events.emit(event)
    }

    override suspend fun publish(events: Collection<DomainEvent>) {
        events.forEach { _events.emit(it) }
    }

    override fun events(): SharedFlow<DomainEvent> = _events.asSharedFlow()
}
