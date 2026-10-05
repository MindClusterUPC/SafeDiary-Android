package com.mindcluster.safediary.assistantai.infrastructure.local

import android.content.Context
import com.mindcluster.safediary.assistantai.domain.model.DiaritoPersonality

class PersonalityPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(): DiaritoPersonality {
        val saved = prefs.getString(KEY_PERSONALITY, null)
        return DiaritoPersonality.fromApiName(saved)
    }

    fun set(p: DiaritoPersonality) {
        prefs.edit().putString(KEY_PERSONALITY, p.apiName).apply()
    }

    companion object {
        private const val PREFS_NAME = "diarito_prefs"
        private const val KEY_PERSONALITY = "personality"
    }
}
