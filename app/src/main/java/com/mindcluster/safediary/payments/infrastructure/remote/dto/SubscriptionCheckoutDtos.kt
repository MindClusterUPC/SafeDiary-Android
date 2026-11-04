package com.mindcluster.safediary.payments.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class CreateSubscriptionCheckoutRequestDto(
    @SerializedName("patientAccountId") val patientAccountId: Long,
    @SerializedName("plan") val plan: String,
    @SerializedName("successUrl") val successUrl: String? = null,
    @SerializedName("cancelUrl") val cancelUrl: String? = null
)

data class SubscriptionCheckoutResponseDto(
    @SerializedName("patientAccountId") val patientAccountId: Long,
    @SerializedName("plan") val plan: String,
    @SerializedName("checkoutUrl") val checkoutUrl: String?,
    @SerializedName("sessionId") val sessionId: String?,
    @SerializedName("checkoutRequired") val checkoutRequired: Boolean,
    @SerializedName("message") val message: String
)

data class CancelSubscriptionRequestDto(
    @SerializedName("patientAccountId") val patientAccountId: Long
)
