package com.mindcluster.safediary.assistantai.domain.model

import com.mindcluster.safediary.shared.domain.model.ValueObject

enum class RiskLevel : ValueObject {
    LOW,
    MODERATE,
    HIGH,
    CRITICAL;

    val requiresCrisisSupport: Boolean
        get() = this == HIGH || this == CRITICAL

    companion object {
        fun fromBackend(value: String?): RiskLevel =
            entries.firstOrNull { it.name == value } ?: LOW
    }
}
