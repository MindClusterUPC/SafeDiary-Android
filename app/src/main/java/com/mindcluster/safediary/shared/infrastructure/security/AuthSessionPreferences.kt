package com.mindcluster.safediary.shared.infrastructure.security

import android.content.Context
import android.content.SharedPreferences

class AuthSessionPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
        set(value) {
            prefs.edit().apply {
                if (value != null) {
                    putString(KEY_AUTH_TOKEN, value)
                } else {
                    remove(KEY_AUTH_TOKEN)
                }
            }.apply()
            AuthSessionHolder.token = value
        }

    var patientAccountId: Long
        get() = prefs.getLong(KEY_PATIENT_ACCOUNT_ID, 1L)
        set(value) = prefs.edit().putLong(KEY_PATIENT_ACCOUNT_ID, value).apply()

    var userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

    init {
        AuthSessionHolder.token = authToken
    }

    fun clear() {
        prefs.edit().clear().apply()
        AuthSessionHolder.token = null
    }

    companion object {
        private const val PREFS_NAME = "safediary_auth_session"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_PATIENT_ACCOUNT_ID = "patient_account_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
}

object AuthSessionHolder {
    @Volatile
    var token: String? = null
}
