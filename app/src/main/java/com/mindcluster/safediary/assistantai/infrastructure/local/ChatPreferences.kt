package com.mindcluster.safediary.assistantai.infrastructure.local

import android.content.Context
import com.mindcluster.safediary.shared.infrastructure.security.AccountStorage
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionPreferences

class ChatPreferences(context: Context, accountId: Long = AuthSessionPreferences(context).patientAccountId) {
    private val prefs = context.getSharedPreferences(AccountStorage.chatPreferencesName(accountId), Context.MODE_PRIVATE)

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
        private const val KEY_LAST_CONVERSATION_ID = "last_opened_conversation_id"
    }
}
