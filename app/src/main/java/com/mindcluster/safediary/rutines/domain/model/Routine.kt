package com.mindcluster.safediary.rutines.domain.model

enum class RoutineKind { BREATHING, GROUNDING }

data class Routine(
    val id: String,
    val title: String,
    val description: String,
    val time: String,
    val minutes: Int,
    val days: Set<Int>,
    val reminder: Boolean = true,
    val completed: Boolean = false,
    val kind: RoutineKind = RoutineKind.GROUNDING
)
