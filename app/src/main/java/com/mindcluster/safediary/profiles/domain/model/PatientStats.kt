package com.mindcluster.safediary.profiles.domain.model

import com.mindcluster.safediary.shared.domain.model.ValueObject

data class PatientStats(
    val streakDays: Int = 14,
    val totalReflections: Int = 42,
    val frequentMood: String = "Serene"
) : ValueObject
