package com.mindcluster.safediary.payments.domain.model

import com.mindcluster.safediary.shared.domain.model.ValueObject

data class PaymentMethodCard(
    val id: String,
    val title: String,
    val brand: String, // VISA, Mastercard, etc.
    val last4: String,
    val cardholder: String,
    val expiry: String,
    val isDefault: Boolean = false,
    val note: String? = null
) : ValueObject
