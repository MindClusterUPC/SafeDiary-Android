package com.mindcluster.safediary.iam.domain.repository

import com.mindcluster.safediary.iam.domain.model.UserSession

interface AuthRepository {
    suspend fun signIn(email: String, password: String): UserSession
    suspend fun signUp(fullName: String, email: String, password: String, role: String): UserSession
    suspend fun signOut()
    fun isLoggedIn(): Boolean
    fun getStoredSession(): UserSession?
}
