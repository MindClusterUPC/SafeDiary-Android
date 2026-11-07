package com.mindcluster.safediary.payments.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class PaymentHistoryItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("amountFormatted") val amountFormatted: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("referenceNumber") val referenceNumber: String,
    @SerializedName("category") val category: String, // "CONSULTATION" or "SUBSCRIPTION"
    @SerializedName("initials") val initials: String?,
    @SerializedName("createdAt") val createdAt: String
)

data class PaymentMethodItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("brand") val brand: String,
    @SerializedName("last4") val last4: String,
    @SerializedName("expiry") val expiry: String,
    @SerializedName("cardholder") val cardholder: String,
    @SerializedName("isDefault") val isDefault: Boolean,
    @SerializedName("note") val note: String?
)
