package com.mindcluster.safediary.shared.domain.model

abstract class Entity<ID> {
    abstract val id: ID

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Entity<*>) return false
        return id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
