package com.mindcluster.safediary.profiles.infrastructure

import com.mindcluster.safediary.profiles.domain.model.*
import com.mindcluster.safediary.profiles.domain.repository.PatientProfileRepository
import com.mindcluster.safediary.profiles.infrastructure.local.ProfilePreferences
import com.mindcluster.safediary.profiles.infrastructure.remote.RetrofitPatientProfileGateway
import com.mindcluster.safediary.profiles.infrastructure.remote.dto.UpdateEmergencyContactRequestDto
import com.mindcluster.safediary.shared.domain.events.DomainEventPublisher
import com.mindcluster.safediary.shared.infrastructure.network.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OfflineFirstPatientProfileRepository(
    private val preferences: ProfilePreferences,
    private val remoteGateway: RetrofitPatientProfileGateway,
    private val networkMonitor: NetworkMonitor,
    private val eventPublisher: DomainEventPublisher
) : PatientProfileRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val profileState = MutableStateFlow<PatientProfileAggregate?>(null)

    init {
        // Load initial profile from local preferences
        val initial = buildFromPreferences(1L)
        profileState.value = initial
    }

    override fun observeProfile(patientId: Long): Flow<PatientProfileAggregate?> =
        profileState.asStateFlow()

    override suspend fun getProfile(patientId: Long): PatientProfileAggregate {
        // Synchronously fetch from backend when online before returning
        if (networkMonitor.isOnline.value) {
            try {
                val remoteDto = remoteGateway.fetchPatientProfile(patientId)
                if (remoteDto != null) {
                    preferences.firstName = remoteDto.firstName
                    preferences.lastName = remoteDto.lastName
                    preferences.email = remoteDto.email

                    if (remoteDto.emergencyContact != null) {
                        val ec = remoteDto.emergencyContact
                        val rel = RelationshipType.fromString(ec.relationship)
                        preferences.hasEmergencyContact = true
                        preferences.emergencyName = ec.name
                        preferences.emergencyRelationship = rel.name
                        preferences.emergencyPhone = ec.phoneNumber
                        preferences.emergencyEmail = ec.email
                    } else {
                        preferences.hasEmergencyContact = false
                        preferences.emergencyName = null
                        preferences.emergencyPhone = null
                        preferences.emergencyEmail = null
                    }
                }
            } catch (e: Exception) {
                // Fall back to local preferences if network call fails
            }
        }

        val current = buildFromPreferences(patientId)
        profileState.value = current
        return current
    }

    override suspend fun updateName(
        patientId: Long,
        firstName: String,
        lastName: String
    ): PatientProfileAggregate {
        if (networkMonitor.isOnline.value) {
            val response = remoteGateway.updatePatientProfile(patientId, firstName.trim(), lastName.trim())
            if (response != null) {
                preferences.firstName = response.firstName
                preferences.lastName = response.lastName
                preferences.email = response.email
            } else {
                preferences.firstName = firstName.trim()
                preferences.lastName = lastName.trim()
            }
        } else {
            preferences.firstName = firstName.trim()
            preferences.lastName = lastName.trim()
        }

        if (preferences.avatarType == "INITIALS") {
            preferences.avatarValue = PatientProfileAggregate.computeInitials(preferences.firstName, preferences.lastName)
        }
        val updated = buildFromPreferences(patientId)
        profileState.value = updated
        dispatchEvents(updated)
        return updated
    }

    override suspend fun updateEmail(
        patientId: Long,
        newEmail: String,
        currentPassword: String
    ): PatientProfileAggregate {
        if (networkMonitor.isOnline.value) {
            val remoteResult = remoteGateway.updatePatientEmail(patientId, newEmail.trim(), currentPassword)
            if (remoteResult.isFailure) {
                throw remoteResult.exceptionOrNull() ?: RuntimeException("Error al actualizar el correo electrónico")
            }
        }

        preferences.email = newEmail.trim()
        val updated = buildFromPreferences(patientId)
        profileState.value = updated
        dispatchEvents(updated)
        return updated
    }

    override suspend fun updateEmergencyContact(
        patientId: Long,
        contact: EmergencyContact
    ): PatientProfileAggregate {
        if (networkMonitor.isOnline.value) {
            val response = remoteGateway.updateEmergencyContact(
                patientId,
                UpdateEmergencyContactRequestDto(
                    name = contact.name,
                    relationship = contact.relationship.name,
                    phoneNumber = contact.phoneNumber,
                    email = contact.email
                )
            )
            if (response?.emergencyContact != null) {
                val ec = response.emergencyContact
                preferences.hasEmergencyContact = true
                preferences.emergencyName = ec.name
                preferences.emergencyRelationship = RelationshipType.fromString(ec.relationship).name
                preferences.emergencyPhone = ec.phoneNumber
                preferences.emergencyEmail = ec.email
            } else {
                preferences.hasEmergencyContact = true
                preferences.emergencyName = contact.name
                preferences.emergencyRelationship = contact.relationship.name
                preferences.emergencyPhone = contact.phoneNumber
                preferences.emergencyEmail = contact.email
            }
        } else {
            preferences.hasEmergencyContact = true
            preferences.emergencyName = contact.name
            preferences.emergencyRelationship = contact.relationship.name
            preferences.emergencyPhone = contact.phoneNumber
            preferences.emergencyEmail = contact.email
        }
        preferences.notifyOnInactivity = contact.notifyOnExtendedInactivity

        val updated = buildFromPreferences(patientId)
        profileState.value = updated
        dispatchEvents(updated)
        return updated
    }

    override suspend fun removeEmergencyContact(patientId: Long): PatientProfileAggregate {
        if (networkMonitor.isOnline.value) {
            remoteGateway.deleteEmergencyContact(patientId)
        }

        preferences.hasEmergencyContact = false
        preferences.emergencyName = null
        preferences.emergencyPhone = null
        preferences.emergencyEmail = null

        val updated = buildFromPreferences(patientId)
        profileState.value = updated
        dispatchEvents(updated)
        return updated
    }

    override suspend fun updateAvatar(
        patientId: Long,
        config: AvatarConfig
    ): PatientProfileAggregate {
        when (config) {
            is AvatarConfig.Initials -> {
                preferences.avatarType = "INITIALS"
                preferences.avatarValue = config.initials
            }
            is AvatarConfig.CustomImage -> {
                preferences.avatarType = "CUSTOM"
                preferences.avatarValue = config.imageUri
            }
            is AvatarConfig.PresetAvatar -> {
                preferences.avatarType = "PRESET"
                preferences.avatarValue = config.presetId
            }
        }
        val updated = buildFromPreferences(patientId)
        profileState.value = updated
        dispatchEvents(updated)
        return updated
    }

    override suspend fun updateTherapeuticTopics(
        patientId: Long,
        topics: Set<TherapeuticTopic>
    ): PatientProfileAggregate {
        preferences.selectedTopics = topics.map { it.code }.toSet()
        val updated = buildFromPreferences(patientId)
        profileState.value = updated
        dispatchEvents(updated)
        return updated
    }

    override suspend fun updatePrivacySettings(
        patientId: Long,
        biometricLock: Boolean,
        dailyReminder: Boolean
    ): PatientProfileAggregate {
        preferences.biometricLockEnabled = biometricLock
        preferences.dailyReminderEnabled = dailyReminder
        val updated = buildFromPreferences(patientId)
        profileState.value = updated
        dispatchEvents(updated)
        return updated
    }

    private fun buildFromPreferences(patientId: Long): PatientProfileAggregate {
        val first = preferences.firstName
        val last = preferences.lastName
        val avatar: AvatarConfig = when (preferences.avatarType) {
            "CUSTOM" -> AvatarConfig.CustomImage(preferences.avatarValue)
            "PRESET" -> {
                val found = AvatarConfig.DEFAULT_PRESETS.firstOrNull { it.presetId == preferences.avatarValue }
                found ?: AvatarConfig.Initials(PatientProfileAggregate.computeInitials(first, last))
            }
            else -> AvatarConfig.Initials(PatientProfileAggregate.computeInitials(first, last))
        }

        val topics = preferences.selectedTopics.mapNotNull { TherapeuticTopic.fromCode(it) }.toSet()

        val emergency = if (preferences.hasEmergencyContact) {
            preferences.emergencyName?.takeIf { it.isNotBlank() }?.let { name ->
                EmergencyContact(
                    name = name,
                    relationship = RelationshipType.fromString(preferences.emergencyRelationship),
                    phoneNumber = preferences.emergencyPhone,
                    email = preferences.emergencyEmail,
                    notifyOnExtendedInactivity = preferences.notifyOnInactivity
                )
            }
        } else null

        val stats = PatientStats(
            streakDays = preferences.streakDays,
            totalReflections = preferences.totalReflections,
            frequentMood = preferences.frequentMood
        )

        return PatientProfileAggregate(
            id = patientId,
            firstName = first,
            lastName = last,
            email = preferences.email,
            avatarConfig = avatar,
            emergencyContact = emergency,
            selectedTopics = if (topics.isEmpty()) setOf(TherapeuticTopic.ANXIETY_AND_STRESS) else topics,
            biometricLockEnabled = preferences.biometricLockEnabled,
            dailyReminderEnabled = preferences.dailyReminderEnabled,
            stats = stats
        )
    }

    private fun dispatchEvents(aggregate: PatientProfileAggregate) {
        aggregate.getDomainEvents().forEach { event ->
            scope.launch { eventPublisher.publish(event) }
        }
        aggregate.clearEvents()
    }
}
