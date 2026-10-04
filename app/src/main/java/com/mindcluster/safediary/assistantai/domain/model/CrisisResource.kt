package com.mindcluster.safediary.assistantai.domain.model

import com.mindcluster.safediary.shared.domain.model.ValueObject

data class CrisisResource(
    val name: String,
    val phone: String,
    val description: String
) : ValueObject
