package com.mindcluster.safediary.profiles.domain.repository

import com.mindcluster.safediary.profiles.domain.model.AvatarConfig
import com.mindcluster.safediary.profiles.domain.model.EmergencyContact
import com.mindcluster.safediary.profiles.domain.model.PatientProfileAggregate
import com.mindcluster.safediary.profiles.domain.model.TherapeuticTopic
import kotlinx.coroutines.flow.Flow

interface PatientProfileRepository {
    fun observeProfile(patientId: Long): Flow<PatientProfileAggregate?>
    suspend fun getProfile(patientId: Long): PatientProfileAggregate
    suspend fun updateName(patientId: Long, firstName: String, lastName: String): PatientProfileAggregate
    suspend fun updateEmail(patientId: Long, newEmail: String, currentPassword: String): PatientProfileAggregate
    suspend fun updateEmergencyContact(patientId: Long, contact: EmergencyContact): PatientProfileAggregate
    suspend fun removeEmergencyContact(patientId: Long): PatientProfileAggregate
    suspend fun updateAvatar(patientId: Long, config: AvatarConfig): PatientProfileAggregate
    suspend fun updateTherapeuticTopics(patientId: Long, topics: Set<TherapeuticTopic>): PatientProfileAggregate
    suspend fun updatePrivacySettings(patientId: Long, biometricLock: Boolean, dailyReminder: Boolean): PatientProfileAggregate
}
