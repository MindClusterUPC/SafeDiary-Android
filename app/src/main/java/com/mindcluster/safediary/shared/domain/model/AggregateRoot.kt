package com.mindcluster.safediary.shared.domain.model

import com.mindcluster.safediary.shared.domain.events.DomainEvent

abstract class AggregateRoot {
    private val _domainEvents = mutableListOf<DomainEvent>()

    protected fun raise(event: DomainEvent) {
        _domainEvents.add(event)
    }

    fun getDomainEvents(): List<DomainEvent> = _domainEvents.toList()

    fun clearEvents() {
        _domainEvents.clear()
    }
}
