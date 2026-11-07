package com.mindcluster.safediary.payments.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class SubscriptionResponseDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("patientAccountId") val patientAccountId: Long,
    @SerializedName("plan") val plan: String,
    @SerializedName("status") val status: String,
    @SerializedName("activeUntil") val activeUntil: String?,
    @SerializedName("active") val active: Boolean,
    @SerializedName("stripeSubscriptionId") val stripeSubscriptionId: String?
)
