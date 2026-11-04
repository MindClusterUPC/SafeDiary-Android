package com.mindcluster.safediary.profiles.domain.model

import com.mindcluster.safediary.shared.domain.model.ValueObject

enum class TherapeuticTopic(val code: String, val stringKey: String) : ValueObject {
    ANXIETY_AND_STRESS("anxiety_stress", "topic_anxiety_stress"),
    MINDFULNESS("mindfulness", "topic_mindfulness"),
    GRIEF_RECOVERY("grief_recovery", "topic_grief_recovery"),
    PERSONAL_GROWTH("personal_growth", "topic_personal_growth");

    companion object {
        fun fromCode(code: String): TherapeuticTopic? =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
    }
}
