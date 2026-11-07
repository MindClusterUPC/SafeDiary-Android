package com.mindcluster.safediary.profiles.domain.model

import com.mindcluster.safediary.profiles.domain.events.EmergencyContactChangedEvent
import com.mindcluster.safediary.profiles.domain.events.PatientProfileUpdatedEvent
import com.mindcluster.safediary.shared.domain.model.AggregateRoot

class PatientProfileAggregate(
    val id: Long,
    var firstName: String,
    var lastName: String,
    var email: String,
    var avatarConfig: AvatarConfig = AvatarConfig.Initials(computeInitials(firstName, lastName)),
    var emergencyContact: EmergencyContact? = null,
    var selectedTopics: Set<TherapeuticTopic> = setOf(
        TherapeuticTopic.ANXIETY_AND_STRESS,
        TherapeuticTopic.MINDFULNESS,
        TherapeuticTopic.PERSONAL_GROWTH
    ),
    var biometricLockEnabled: Boolean = true,
    var dailyReminderEnabled: Boolean = true,
    var stats: PatientStats = PatientStats()
) : AggregateRoot() {

    val fullName: String
        get() = "$firstName $lastName".trim()

    val initials: String
        get() = computeInitials(firstName, lastName)

    fun updateName(first: String, last: String) {
        require(first.isNotBlank()) { "First name cannot be blank." }
        require(last.isNotBlank()) { "Last name cannot be blank." }
        firstName = first.trim()
        lastName = last.trim()
        if (avatarConfig is AvatarConfig.Initials) {
            avatarConfig = AvatarConfig.Initials(initials)
        }
        raise(PatientProfileUpdatedEvent(patientId = id))
    }

    fun updateEmail(newEmail: String) {
        val trimmed = newEmail.trim()
        require(trimmed.contains("@") && trimmed.contains(".")) { "Invalid email address format." }
        email = trimmed
        raise(PatientProfileUpdatedEvent(patientId = id))
    }

    fun updateAvatar(newConfig: AvatarConfig) {
        avatarConfig = newConfig
        raise(PatientProfileUpdatedEvent(patientId = id))
    }

    fun assignEmergencyContact(contact: EmergencyContact) {
        emergencyContact = contact
        raise(EmergencyContactChangedEvent(patientId = id, isRemoved = false))
    }

    fun removeEmergencyContact() {
        emergencyContact = null
        raise(EmergencyContactChangedEvent(patientId = id, isRemoved = true))
    }

    fun toggleTopic(topic: TherapeuticTopic) {
        selectedTopics = if (selectedTopics.contains(topic)) {
            selectedTopics - topic
        } else {
            selectedTopics + topic
        }
        raise(PatientProfileUpdatedEvent(patientId = id))
    }

    fun updatePrivacySettings(biometricLock: Boolean, dailyReminder: Boolean) {
        biometricLockEnabled = biometricLock
        dailyReminderEnabled = dailyReminder
        raise(PatientProfileUpdatedEvent(patientId = id))
    }

    companion object {
        fun computeInitials(firstName: String, lastName: String): String {
            val f = firstName.trim().firstOrNull()?.uppercaseChar() ?: 'E'
            val l = lastName.trim().firstOrNull()?.uppercaseChar() ?: 'V'
            return "$f$l"
        }
    }
}
