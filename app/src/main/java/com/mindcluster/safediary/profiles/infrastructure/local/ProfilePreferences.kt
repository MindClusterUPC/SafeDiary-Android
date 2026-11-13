package com.mindcluster.safediary.profiles.infrastructure.local

import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionPreferences

import android.content.Context
import android.content.SharedPreferences

class ProfilePreferences(context: Context) {
    private val appContext = context.applicationContext
    private val avatarPrefs: SharedPreferences
        get() {
            val id = AuthSessionPreferences(appContext).patientAccountId
            return appContext.getSharedPreferences("safediary_avatar_account_$id", Context.MODE_PRIVATE)
        }
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val initials: String
        get() = (firstName.trim().firstOrNull()?.uppercase().orEmpty() +
                lastName.trim().firstOrNull()?.uppercase().orEmpty()).ifBlank { "SD" }

    fun observeChanges(onChange: () -> Unit): () -> Unit {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }
        val observedAvatarPrefs = avatarPrefs
        prefs.registerOnSharedPreferenceChangeListener(listener)
        observedAvatarPrefs.registerOnSharedPreferenceChangeListener(listener)
        return {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
            observedAvatarPrefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    var firstName: String
        get() = prefs.getString(KEY_FIRST_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_FIRST_NAME, value).apply()

    var lastName: String
        get() = prefs.getString(KEY_LAST_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_NAME, value).apply()

    var email: String
        get() = prefs.getString(KEY_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_EMAIL, value).apply()

    var avatarType: String
        get() = avatarPrefs.getString(KEY_AVATAR_TYPE, "INITIALS") ?: "INITIALS"
        set(value) = avatarPrefs.edit().putString(KEY_AVATAR_TYPE, value).apply()

    var avatarValue: String
        get() = avatarPrefs.getString(KEY_AVATAR_VALUE, "") ?: ""
        set(value) = avatarPrefs.edit().putString(KEY_AVATAR_VALUE, value).apply()

    var selectedTopics: Set<String>
        get() = prefs.getStringSet(
            KEY_TOPICS,
            emptySet()
        ) ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_TOPICS, value).apply()

    var biometricLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_LOCK, true)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK, value).apply()

    var dailyReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_DAILY_REMINDER, false)
        set(value) = prefs.edit().putBoolean(KEY_DAILY_REMINDER, value).apply()

    var streakDays: Int
        get() = prefs.getInt(KEY_STREAK, 0)
        set(value) = prefs.edit().putInt(KEY_STREAK, value).apply()

    var totalReflections: Int
        get() = prefs.getInt(KEY_REFLECTIONS, 0)
        set(value) = prefs.edit().putInt(KEY_REFLECTIONS, value).apply()

    var frequentMood: String
        get() = prefs.getString(KEY_FREQUENT_MOOD, "") ?: ""
        set(value) = prefs.edit().putString(KEY_FREQUENT_MOOD, value).apply()

    var hasEmergencyContact: Boolean
        get() = prefs.getBoolean(KEY_HAS_EMERGENCY, false)
        set(value) = prefs.edit().putBoolean(KEY_HAS_EMERGENCY, value).apply()

    var emergencyName: String?
        get() = if (hasEmergencyContact) prefs.getString(KEY_EMERGENCY_NAME, null) else null
        set(value) = prefs.edit().putString(KEY_EMERGENCY_NAME, value).apply()

    var emergencyRelationship: String
        get() = prefs.getString(KEY_EMERGENCY_REL, "OTHER") ?: "OTHER"
        set(value) = prefs.edit().putString(KEY_EMERGENCY_REL, value).apply()

    var emergencyPhone: String?
        get() = if (hasEmergencyContact) prefs.getString(KEY_EMERGENCY_PHONE, null) else null
        set(value) = prefs.edit().putString(KEY_EMERGENCY_PHONE, value).apply()

    var emergencyEmail: String?
        get() = if (hasEmergencyContact) prefs.getString(KEY_EMERGENCY_EMAIL, null) else null
        set(value) = prefs.edit().putString(KEY_EMERGENCY_EMAIL, value).apply()

    var notifyOnInactivity: Boolean
        get() = prefs.getBoolean(KEY_NOTIFY_INACTIVITY, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFY_INACTIVITY, value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "safediary_patient_profile"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_AVATAR_TYPE = "avatar_type"
        private const val KEY_AVATAR_VALUE = "avatar_value"
        private const val KEY_TOPICS = "topics"
        private const val KEY_BIOMETRIC_LOCK = "biometric_lock"
        private const val KEY_DAILY_REMINDER = "daily_reminder"
        private const val KEY_STREAK = "streak_days"
        private const val KEY_REFLECTIONS = "reflections_count"
        private const val KEY_FREQUENT_MOOD = "frequent_mood"
        private const val KEY_HAS_EMERGENCY = "has_emergency_contact"
        private const val KEY_EMERGENCY_NAME = "emergency_name"
        private const val KEY_EMERGENCY_REL = "emergency_rel"
        private const val KEY_EMERGENCY_PHONE = "emergency_phone"
        private const val KEY_EMERGENCY_EMAIL = "emergency_email"
        private const val KEY_NOTIFY_INACTIVITY = "notify_inactivity"
    }
}
