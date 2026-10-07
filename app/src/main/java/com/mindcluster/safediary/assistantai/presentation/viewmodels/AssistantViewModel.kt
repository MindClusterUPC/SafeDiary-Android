package com.mindcluster.safediary.assistantai.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mindcluster.safediary.assistantai.application.commands.DeleteConversationCommand
import com.mindcluster.safediary.assistantai.application.commands.DeleteConversationHandler
import com.mindcluster.safediary.assistantai.application.commands.DismissCrisisSupportHandler
import com.mindcluster.safediary.assistantai.application.commands.EditMessageCommand
import com.mindcluster.safediary.assistantai.application.commands.EditMessageHandler
import com.mindcluster.safediary.assistantai.application.commands.OpenConversationCommand
import com.mindcluster.safediary.assistantai.application.commands.OpenConversationHandler
import com.mindcluster.safediary.assistantai.application.commands.RegenerateReplyCommand
import com.mindcluster.safediary.assistantai.application.commands.RegenerateReplyHandler
import com.mindcluster.safediary.assistantai.application.commands.RenameConversationCommand
import com.mindcluster.safediary.assistantai.application.commands.RenameConversationHandler
import com.mindcluster.safediary.assistantai.application.commands.RetryLastPromptHandler
import com.mindcluster.safediary.assistantai.application.commands.SendPromptCommand
import com.mindcluster.safediary.assistantai.application.commands.SendPromptHandler
import com.mindcluster.safediary.assistantai.application.commands.StartNewChatHandler
import com.mindcluster.safediary.assistantai.application.queries.GetChatHistoryHandler
import com.mindcluster.safediary.assistantai.application.queries.GetConversationListHandler
import com.mindcluster.safediary.assistantai.application.queries.GetCrisisSupportHandler
import com.mindcluster.safediary.assistantai.domain.model.ChatMessage
import com.mindcluster.safediary.assistantai.domain.model.ConversationSummary
import com.mindcluster.safediary.assistantai.domain.model.CrisisResource
import com.mindcluster.safediary.assistantai.domain.model.MessageAuthor
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.mindcluster.safediary.assistantai.application.commands.OfflineException
import com.mindcluster.safediary.assistantai.infrastructure.local.ChatPreferences
import com.mindcluster.safediary.shared.infrastructure.network.NetworkMonitor

data class AssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isTyping: Boolean = false,
    val replyFailed: Boolean = false,
    val crisisResources: List<CrisisResource> = emptyList(),
    val conversations: List<ConversationSummary> = emptyList(),
    val isHistoryLoading: Boolean = false,
    val historyFailed: Boolean = false,
    val activeConversationId: String? = null,
    val editingMessageRemoteId: Long? = null,
    val isOnline: Boolean = true,
    val showOfflinePendingNotice: Boolean = false
)

class AssistantViewModel(
    private val sendPromptHandler: SendPromptHandler,
    private val retryLastPromptHandler: RetryLastPromptHandler,
    private val getChatHistoryHandler: GetChatHistoryHandler,
    private val startNewChatHandler: StartNewChatHandler,
    private val getCrisisSupportHandler: GetCrisisSupportHandler,
    private val dismissCrisisSupportHandler: DismissCrisisSupportHandler,
    private val getConversationListHandler: GetConversationListHandler,
    private val openConversationHandler: OpenConversationHandler,
    private val editMessageHandler: EditMessageHandler,
    private val regenerateReplyHandler: RegenerateReplyHandler,
    private val renameConversationHandler: RenameConversationHandler,
    private val deleteConversationHandler: DeleteConversationHandler,
    private val networkMonitor: NetworkMonitor? = null,
    private val chatPreferences: ChatPreferences? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AssistantUiState(
            isOnline = networkMonitor?.isCurrentlyOnline() ?: true
        )
    )
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getChatHistoryHandler.handle().collect { list ->
                _uiState.update { state ->
                    val hasPending = list.any { it.isPending }
                    state.copy(
                        messages = list,
                        showOfflinePendingNotice = if (!hasPending) false else state.showOfflinePendingNotice
                    )
                }
                // Messages left pending from a previous session are sent as soon as we are online.
                if (list.any { it.isPending } && networkMonitor?.isCurrentlyOnline() != false) {
                    retryPendingMessages()
                }
            }
        }
        viewModelScope.launch {
            getCrisisSupportHandler.handle().collect { resources ->
                _uiState.update { it.copy(crisisResources = resources) }
            }
        }
        viewModelScope.launch {
            getConversationListHandler.observe().collect { list ->
                if (list.isNotEmpty() || !_uiState.value.isHistoryLoading) {
                    _uiState.update { it.copy(conversations = list) }
                }
            }
        }
        chatPreferences?.getLastOpenedConversationId()?.let { lastId ->
            _uiState.update { it.copy(activeConversationId = lastId) }
        }
        networkMonitor?.let { monitor ->
            viewModelScope.launch {
                monitor.isOnline.collect { online ->
                    val wasOffline = !_uiState.value.isOnline
                    _uiState.update { it.copy(isOnline = online) }
                    if (online && wasOffline) {
                        retryPendingMessages()
                        refreshHistory()
                    }
                }
            }
        }
    }

    fun refreshHistory() {
        if (_uiState.value.isHistoryLoading) return
        _uiState.update { it.copy(isHistoryLoading = true) }
        viewModelScope.launch {
            val result = getConversationListHandler.handle()
            _uiState.update { state ->
                state.copy(
                    isHistoryLoading = false,
                    historyFailed = result.isFailure,
                    conversations = result.getOrDefault(state.conversations)
                )
            }
        }
    }

    fun openConversation(remoteId: String) {
        if (_uiState.value.isTyping) return
        _uiState.update { it.copy(isTyping = true, replyFailed = false, input = "", editingMessageRemoteId = null) }
        chatPreferences?.setLastOpenedConversationId(remoteId)
        viewModelScope.launch {
            val result = openConversationHandler.handle(OpenConversationCommand(remoteId))
            _uiState.update {
                it.copy(
                    isTyping = false,
                    replyFailed = false,
                    activeConversationId = if (result.isSuccess) remoteId else it.activeConversationId
                )
            }
        }
    }

    fun dismissCrisisSupport() {
        viewModelScope.launch {
            dismissCrisisSupportHandler.handle()
        }
    }

    fun dismissPendingNotice() {
        _uiState.update { it.copy(showOfflinePendingNotice = false) }
    }

    fun onInputChanged(newInput: String) {
        _uiState.update { it.copy(input = newInput) }
    }

    fun startEditing(message: ChatMessage) {
        if (_uiState.value.isTyping) return
        val remoteId = message.remoteId ?: return
        _uiState.update {
            it.copy(
                input = message.content,
                editingMessageRemoteId = remoteId
            )
        }
    }

    fun cancelEditing() {
        _uiState.update {
            it.copy(
                input = "",
                editingMessageRemoteId = null
            )
        }
    }

    fun send() {
        val prompt = _uiState.value.input.trim()
        if (prompt.isBlank() || _uiState.value.isTyping) return

        val isOffline = networkMonitor?.isCurrentlyOnline() == false

        val editingId = _uiState.value.editingMessageRemoteId
        if (editingId != null) {
            _uiState.update {
                it.copy(
                    input = "",
                    editingMessageRemoteId = null,
                    isTyping = true,
                    replyFailed = false
                )
            }
            viewModelScope.launch {
                val result = editMessageHandler.handle(EditMessageCommand(editingId, prompt))
                _uiState.update {
                    it.copy(
                        isTyping = false,
                        replyFailed = result.isFailure,
                        activeConversationId = result.getOrNull() ?: it.activeConversationId
                    )
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    input = "",
                    isTyping = !isOffline,
                    replyFailed = false,
                    showOfflinePendingNotice = isOffline
                )
            }
            viewModelScope.launch {
                val result = sendPromptHandler.handle(SendPromptCommand(prompt))
                val isOfflineFailure = result.exceptionOrNull() is OfflineException
                _uiState.update {
                    it.copy(
                        isTyping = false,
                        replyFailed = result.isFailure && !isOfflineFailure,
                        showOfflinePendingNotice = isOfflineFailure,
                        activeConversationId = result.getOrNull() ?: it.activeConversationId
                    )
                }
            }
        }
    }

    private var pendingRetryJob: Job? = null

    fun retryPendingMessages() {
        // The history flow emits on every save, so only one retry may run at a time.
        if (pendingRetryJob?.isActive == true) return
        pendingRetryJob = viewModelScope.launch {
            val result = sendPromptHandler.sendPending()
            if (result.isSuccess) {
                _uiState.update { it.copy(showOfflinePendingNotice = false) }
            }
        }
    }

    fun regenerateLastReply() {
        if (_uiState.value.isTyping) return
        val lastMsg = _uiState.value.messages.lastOrNull() ?: return
        if (lastMsg.author != MessageAuthor.ASSISTANT || lastMsg.remoteId == null) return

        _uiState.update { it.copy(isTyping = true, replyFailed = false) }
        viewModelScope.launch {
            val result = regenerateReplyHandler.handle(RegenerateReplyCommand())
            _uiState.update {
                it.copy(
                    isTyping = false,
                    replyFailed = result.isFailure,
                    activeConversationId = result.getOrNull() ?: it.activeConversationId
                )
            }
        }
    }

    fun renameConversation(remoteId: String, newTitle: String) {
        val trimmed = newTitle.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val result = renameConversationHandler.handle(RenameConversationCommand(remoteId, trimmed))
            if (result.isSuccess) {
                _uiState.update { state ->
                    state.copy(
                        conversations = state.conversations.map {
                            if (it.remoteId == remoteId) it.copy(title = trimmed) else it
                        }
                    )
                }
            }
        }
    }

    fun deleteConversation(remoteId: String) {
        val isDeletingActive = _uiState.value.activeConversationId == remoteId
        if (isDeletingActive) {
            chatPreferences?.setLastOpenedConversationId(null)
        }
        viewModelScope.launch {
            val result = deleteConversationHandler.handle(DeleteConversationCommand(remoteId))
            if (result.isSuccess) {
                _uiState.update { state ->
                    state.copy(
                        conversations = state.conversations.filterNot { it.remoteId == remoteId },
                        activeConversationId = if (isDeletingActive) null else state.activeConversationId,
                        input = if (isDeletingActive) "" else state.input,
                        editingMessageRemoteId = if (isDeletingActive) null else state.editingMessageRemoteId
                    )
                }
            }
        }
    }

    fun retry() {
        if (_uiState.value.isTyping) return
        _uiState.update { it.copy(isTyping = true, replyFailed = false) }
        viewModelScope.launch {
            val result = retryLastPromptHandler.handle()
            _uiState.update {
                it.copy(
                    isTyping = false,
                    replyFailed = result.isFailure,
                    activeConversationId = result.getOrNull() ?: it.activeConversationId
                )
            }
        }
    }

    fun onSuggestionSelected(suggestion: String) {
        _uiState.update { it.copy(input = suggestion) }
        send()
    }

    fun newChat() {
        chatPreferences?.setLastOpenedConversationId(null)
        viewModelScope.launch {
            startNewChatHandler.handle()
            _uiState.update {
                it.copy(
                    input = "",
                    isTyping = false,
                    replyFailed = false,
                    activeConversationId = null,
                    editingMessageRemoteId = null,
                    showOfflinePendingNotice = false
                )
            }
        }
    }

    class Factory(
        private val sendPromptHandler: SendPromptHandler,
        private val retryLastPromptHandler: RetryLastPromptHandler,
        private val getChatHistoryHandler: GetChatHistoryHandler,
        private val startNewChatHandler: StartNewChatHandler,
        private val getCrisisSupportHandler: GetCrisisSupportHandler,
        private val dismissCrisisSupportHandler: DismissCrisisSupportHandler,
        private val getConversationListHandler: GetConversationListHandler,
        private val openConversationHandler: OpenConversationHandler,
        private val editMessageHandler: EditMessageHandler,
        private val regenerateReplyHandler: RegenerateReplyHandler,
        private val renameConversationHandler: RenameConversationHandler,
        private val deleteConversationHandler: DeleteConversationHandler,
        private val networkMonitor: NetworkMonitor? = null,
        private val chatPreferences: ChatPreferences? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AssistantViewModel(
                sendPromptHandler,
                retryLastPromptHandler,
                getChatHistoryHandler,
                startNewChatHandler,
                getCrisisSupportHandler,
                dismissCrisisSupportHandler,
                getConversationListHandler,
                openConversationHandler,
                editMessageHandler,
                regenerateReplyHandler,
                renameConversationHandler,
                deleteConversationHandler,
                networkMonitor,
                chatPreferences
            ) as T
        }
    }
}
