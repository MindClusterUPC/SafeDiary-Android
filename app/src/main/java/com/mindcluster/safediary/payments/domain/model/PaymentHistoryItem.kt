package com.mindcluster.safediary.payments.domain.model

import com.mindcluster.safediary.shared.domain.model.ValueObject

enum class PaymentCategory : ValueObject {
    CONSULTATION,
    SUBSCRIPTION
}

data class PaymentHistoryItem(
    val id: String,
    val title: String,
    val description: String,
    val amountFormatted: String, // e.g. "-$75.00"
    val referenceNumber: String, // e.g. "Invoice #SD-94812 • Verified ✓"
    val category: PaymentCategory,
    val initials: String? = null // for avatar badge, e.g. "MV"
) : ValueObject
