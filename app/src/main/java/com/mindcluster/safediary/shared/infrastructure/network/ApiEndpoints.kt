package com.mindcluster.safediary.shared.infrastructure.network

import com.mindcluster.safediary.BuildConfig

/**
 * Centralized API endpoints configured via BuildConfig (which loads environment variables or Gradle properties).
 */
object ApiEndpoints {
    val baseUrl: String = BuildConfig.API_BASE_URL

    val authSignIn: String = BuildConfig.ENDPOINT_AUTH_SIGN_IN
    val patients: String = BuildConfig.ENDPOINT_PATIENTS

    val paymentsCheckout: String = BuildConfig.ENDPOINT_PAYMENTS_CHECKOUT
    val paymentsCurrent: String = BuildConfig.ENDPOINT_PAYMENTS_CURRENT
    val paymentsCancel: String = BuildConfig.ENDPOINT_PAYMENTS_CANCEL
    val paymentsHistory: String = BuildConfig.ENDPOINT_PAYMENTS_HISTORY
    val paymentsMethods: String = BuildConfig.ENDPOINT_PAYMENTS_METHODS

    val stripeDemoUrl: String = BuildConfig.STRIPE_DEMO_URL

    fun patientById(id: Long): String = "$patients/$id"
    fun patientEmergencyContact(id: Long): String = "$patients/$id/emergency-contact"
    fun patientEmail(id: Long): String = "$patients/$id/email"
}
