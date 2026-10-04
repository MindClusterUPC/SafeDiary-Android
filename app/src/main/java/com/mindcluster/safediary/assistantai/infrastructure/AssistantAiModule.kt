package com.mindcluster.safediary.assistantai.infrastructure

import android.content.Context
import com.mindcluster.safediary.assistantai.application.commands.SendPromptHandler
import com.mindcluster.safediary.assistantai.application.queries.GetChatHistoryHandler
import com.mindcluster.safediary.assistantai.domain.model.AssistantResponder
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository
import com.mindcluster.safediary.shared.domain.events.DomainEventPublisher
import com.mindcluster.safediary.shared.infrastructure.eventbus.InMemoryDomainEventBus

class AssistantAiModule(context: Context) {
    val eventBus: DomainEventPublisher by lazy { InMemoryDomainEventBus() }
    val repository: ConversationRepository by lazy { InMemoryConversationRepository() }
    val responder: AssistantResponder by lazy { MockAssistantResponder(context.applicationContext) }

    val sendPromptHandler: SendPromptHandler by lazy {
        SendPromptHandler(repository, responder, eventBus)
    }

    val getChatHistoryHandler: GetChatHistoryHandler by lazy {
        GetChatHistoryHandler(repository)
    }

    companion object {
        @Volatile
        private var instance: AssistantAiModule? = null

        fun getInstance(context: Context): AssistantAiModule {
            return instance ?: synchronized(this) {
                instance ?: AssistantAiModule(context.applicationContext).also { instance = it }
            }
        }
    }
}
