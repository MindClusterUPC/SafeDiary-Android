package com.mindcluster.safediary.shared.infrastructure.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthSessionHolder {
    @Volatile private var currentToken: String? = null
    private val _requiresSignIn = MutableStateFlow(false)
    val requiresSignIn = _requiresSignIn.asStateFlow()

    var token: String?
        get() = currentToken
        set(value) = synchronized(this) {
            currentToken = value
            if (value != null) _requiresSignIn.value = false
        }

    fun restoreToken(value: String?) = synchronized(this) {
        if (!_requiresSignIn.value && currentToken == null) currentToken = value
    }

    /** A late response from an old session must not sign out a newly authenticated account. */
    fun rejectToken(rejectedToken: String?) = synchronized(this) {
        if (!rejectedToken.isNullOrBlank() && rejectedToken == currentToken) {
            currentToken = null
            _requiresSignIn.value = true
        }
    }

    fun rejectStoredSession() = synchronized(this) {
        currentToken = null
        _requiresSignIn.value = true
    }

    fun acknowledgeSignInRequired() {
        _requiresSignIn.value = false
    }
}
