package com.mindcluster.safediary.payments.domain.model

import com.mindcluster.safediary.shared.domain.model.ValueObject

enum class SubscriptionStatus : ValueObject {
    ACTIVE,
    PENDING_PAYMENT,
    CANCELLED,
    EXPIRED;

    companion object {
        fun fromString(value: String?): SubscriptionStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: CANCELLED
    }
}
