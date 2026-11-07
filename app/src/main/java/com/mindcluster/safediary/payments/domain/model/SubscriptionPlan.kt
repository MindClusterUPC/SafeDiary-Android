package com.mindcluster.safediary.payments.domain.model

import com.mindcluster.safediary.shared.domain.model.ValueObject

enum class SubscriptionPlan(val displayName: String, val price: String) : ValueObject {
    FREE("Free Sanctuary", "$0.00"),
    TERRA("Terra Monthly", "$19.99 / mo"),
    ASTRUM("Astrum Annual", "$100.00 / yr");

    companion object {
        fun fromString(value: String?): SubscriptionPlan =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: FREE
    }
}
