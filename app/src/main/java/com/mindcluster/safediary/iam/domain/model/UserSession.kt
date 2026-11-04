package com.mindcluster.safediary.iam.domain.model

import com.mindcluster.safediary.shared.domain.model.ValueObject

data class UserSession(
    val accountId: Long,
    val email: String,
    val token: String,
    val role: String = "PATIENT"
) : ValueObject
