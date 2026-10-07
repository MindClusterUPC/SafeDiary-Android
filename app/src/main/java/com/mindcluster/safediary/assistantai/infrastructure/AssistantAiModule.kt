package com.mindcluster.safediary.assistantai.infrastructure

import android.content.Context
import com.mindcluster.safediary.assistantai.application.commands.DeleteConversationHandler
import com.mindcluster.safediary.assistantai.application.commands.DismissCrisisSupportHandler
import com.mindcluster.safediary.assistantai.application.commands.EditMessageHandler
import com.mindcluster.safediary.assistantai.application.commands.OpenConversationHandler
import com.mindcluster.safediary.assistantai.application.commands.RegenerateReplyHandler
import com.mindcluster.safediary.assistantai.application.commands.RenameConversationHandler
import com.mindcluster.safediary.assistantai.application.commands.RetryLastPromptHandler
import com.mindcluster.safediary.assistantai.application.commands.SendPromptHandler
import com.mindcluster.safediary.assistantai.application.commands.StartNewChatHandler
import com.mindcluster.safediary.assistantai.application.queries.GetChatHistoryHandler
import com.mindcluster.safediary.assistantai.application.queries.GetConversationListHandler
import com.mindcluster.safediary.assistantai.application.queries.GetCrisisSupportHandler
import com.mindcluster.safediary.assistantai.domain.model.AssistantResponder
import com.mindcluster.safediary.assistantai.domain.repository.ConversationHistoryGateway
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository
import com.mindcluster.safediary.shared.domain.events.DomainEventPublisher
import com.mindcluster.safediary.shared.infrastructure.eventbus.InMemoryDomainEventBus
import com.mindcluster.safediary.assistantai.infrastructure.local.CacheFirstConversationHistoryGateway
import com.mindcluster.safediary.assistantai.infrastructure.local.ChatPreferences
import com.mindcluster.safediary.assistantai.infrastructure.local.OfflineFirstConversationRepository
import com.mindcluster.safediary.assistantai.infrastructure.local.SafeDiaryDatabase
import com.mindcluster.safediary.assistantai.infrastructure.remote.RetrofitAssistantResponder
import com.mindcluster.safediary.assistantai.infrastructure.remote.RetrofitConversationHistoryGateway
import com.mindcluster.safediary.shared.infrastructure.network.NetworkMonitor

class AssistantAiModule(context: Context) {
    val database: SafeDiaryDatabase by lazy { SafeDiaryDatabase.getInstance(context) }
    val networkMonitor: NetworkMonitor by lazy { NetworkMonitor.getInstance(context) }
    val chatPreferences: ChatPreferences by lazy { ChatPreferences(context) }

    val eventBus: DomainEventPublisher by lazy { InMemoryDomainEventBus() }
    val repository: ConversationRepository by lazy {
        OfflineFirstConversationRepository(
            database.conversationDao(),
            database.messageDao(),
            chatPreferences
        )
    }
    val responder: AssistantResponder by lazy { RetrofitAssistantResponder(context.applicationContext) }

    val historyGateway: ConversationHistoryGateway by lazy {
        CacheFirstConversationHistoryGateway(
            database.conversationDao(),
            database.messageDao(),
            RetrofitConversationHistoryGateway(),
            networkMonitor
        )
    }

    val sendPromptHandler: SendPromptHandler by lazy {
        SendPromptHandler(repository, responder, eventBus, historyGateway, networkMonitor)
    }

    val retryLastPromptHandler: RetryLastPromptHandler by lazy {
        RetryLastPromptHandler(repository, responder, eventBus, historyGateway)
    }

    val startNewChatHandler: StartNewChatHandler by lazy {
        StartNewChatHandler(repository)
    }

    val getChatHistoryHandler: GetChatHistoryHandler by lazy {
        GetChatHistoryHandler(repository)
    }

    val getConversationListHandler: GetConversationListHandler by lazy {
        GetConversationListHandler(historyGateway)
    }

    val openConversationHandler: OpenConversationHandler by lazy {
        OpenConversationHandler(historyGateway, repository)
    }

    val getCrisisSupportHandler: GetCrisisSupportHandler by lazy {
        GetCrisisSupportHandler(repository)
    }

    val dismissCrisisSupportHandler: DismissCrisisSupportHandler by lazy {
        DismissCrisisSupportHandler(repository)
    }

    val editMessageHandler: EditMessageHandler by lazy {
        EditMessageHandler(repository, responder, historyGateway, eventBus)
    }

    val regenerateReplyHandler: RegenerateReplyHandler by lazy {
        RegenerateReplyHandler(repository, responder, historyGateway, eventBus)
    }

    val renameConversationHandler: RenameConversationHandler by lazy {
        RenameConversationHandler(historyGateway)
    }

    val deleteConversationHandler: DeleteConversationHandler by lazy {
        DeleteConversationHandler(historyGateway, repository)
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
