package com.mindcluster.safediary.professionals.domain.model

data class Professional(
    val id: String,
    val name: String,
    val initials: String,
    val specialty: String,
    val tags: List<String>,
    val approach: String,
    val price: Int,
    val rating: String,
    val reviewCount: Int,
    val availability: String,
    val languages: String = "Español"
)
