package com.mindcluster.safediary.profiles.domain.model

import com.mindcluster.safediary.shared.domain.model.ValueObject

enum class RelationshipType(val labelKey: String) : ValueObject {
    PARENT("relationship_parent"),
    SPOUSE("relationship_spouse"),
    PARTNER("relationship_partner"),
    SIBLING("relationship_sibling"),
    CHILD("relationship_child"),
    FRIEND("relationship_friend"),
    GUARDIAN("relationship_guardian"),
    DOCTOR("relationship_doctor"),
    OTHER("relationship_other");

    companion object {
        fun fromString(value: String?): RelationshipType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}
