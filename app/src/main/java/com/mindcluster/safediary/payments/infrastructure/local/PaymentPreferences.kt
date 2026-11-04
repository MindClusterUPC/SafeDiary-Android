package com.mindcluster.safediary.payments.infrastructure.local

import android.content.Context
import android.content.SharedPreferences

class PaymentPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var currentPlan: String
        get() = prefs.getString(KEY_PLAN, "TERRA") ?: "TERRA"
        set(value) = prefs.edit().putString(KEY_PLAN, value).apply()

    var subscriptionStatus: String
        get() = prefs.getString(KEY_STATUS, "ACTIVE") ?: "ACTIVE"
        set(value) = prefs.edit().putString(KEY_STATUS, value).apply()

    var autoDebitEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_DEBIT, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_DEBIT, value).apply()

    var requireFaceIdForCharges: Boolean
        get() = prefs.getBoolean(KEY_REQUIRE_FACEID, true)
        set(value) = prefs.edit().putBoolean(KEY_REQUIRE_FACEID, value).apply()

    var defaultCardId: String
        get() = prefs.getString(KEY_DEFAULT_CARD, "pm_default") ?: "pm_default"
        set(value) = prefs.edit().putString(KEY_DEFAULT_CARD, value).apply()

    companion object {
        private const val PREFS_NAME = "safediary_payment_prefs"
        private const val KEY_PLAN = "current_plan"
        private const val KEY_STATUS = "subscription_status"
        private const val KEY_AUTO_DEBIT = "auto_debit"
        private const val KEY_REQUIRE_FACEID = "require_faceid"
        private const val KEY_DEFAULT_CARD = "default_card"
    }
}
