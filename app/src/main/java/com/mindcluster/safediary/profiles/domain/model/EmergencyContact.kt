package com.mindcluster.safediary.profiles.domain.model

import com.mindcluster.safediary.shared.domain.model.ValueObject

data class EmergencyContact(
    val name: String,
    val relationship: RelationshipType,
    val phoneNumber: String?,
    val email: String?,
    val notifyOnExtendedInactivity: Boolean = true
) : ValueObject {
    init {
        require(name.isNotBlank()) { "Emergency contact name cannot be blank." }
        require(!phoneNumber.isNullOrBlank() || !email.isNullOrBlank()) {
            "At least one contact method (phone number or email) must be provided."
        }
    }
}
