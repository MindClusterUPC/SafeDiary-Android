package com.mindcluster.safediary.professionals.domain.model

data class Professional(
    val id: String,
    val name: String,
    val initials: String,
    val specialty: String,
    val tags: List<String>,
    val approach: String,
    val price: Int,
    val currency: String = "PEN",
    val durationMinutes: Int = 50,
    /** Average rating already formatted ("4.8"); null while the clinician has no reviews. */
    val rating: String? = null,
    val reviewCount: Int = 0
) {
    val priceLabel: String get() = if (currency == "PEN") "S/ $price" else "$currency $price"
}
