package com.mindcluster.safediary.assistantai.infrastructure.local

import android.content.Context

class ChatPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLastOpenedConversationId(): String? = prefs.getString(KEY_LAST_CONVERSATION_ID, null)

    fun setLastOpenedConversationId(id: String?) {
        prefs.edit().apply {
            if (id != null) {
                putString(KEY_LAST_CONVERSATION_ID, id)
            } else {
                remove(KEY_LAST_CONVERSATION_ID)
            }
        }.apply()
    }

    companion object {
        private const val PREFS_NAME = "diarito_prefs"
        private const val KEY_LAST_CONVERSATION_ID = "last_opened_conversation_id"
    }
}
