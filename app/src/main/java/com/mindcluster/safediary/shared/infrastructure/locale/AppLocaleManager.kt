package com.mindcluster.safediary.shared.infrastructure.locale

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

enum class AppLanguage(val code: String, val displayName: String) {
    ES("es", "Español"),
    EN("en", "English");

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ES
        }
    }
}

object AppLocaleManager {
    private const val PREFS_NAME = "safediary_locale_prefs"
    private const val KEY_LANG = "selected_language"

    fun current(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LANG, null)
        if (saved != null) {
            return AppLanguage.fromCode(saved)
        }
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (!currentLocales.isEmpty) {
            val tag = currentLocales.get(0)?.language
            return AppLanguage.fromCode(tag)
        }
        return AppLanguage.ES
    }

    fun set(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, language.code).apply()
        val appLocale = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun ensureDefault(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_LANG)) {
            set(context, AppLanguage.ES)
        } else {
            val code = prefs.getString(KEY_LANG, "es") ?: "es"
            val appLocale = LocaleListCompat.forLanguageTags(code)
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}
