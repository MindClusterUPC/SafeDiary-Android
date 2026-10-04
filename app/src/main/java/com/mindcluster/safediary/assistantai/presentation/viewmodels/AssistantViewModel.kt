package com.mindcluster.safediary.assistantai.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mindcluster.safediary.assistantai.application.commands.DismissCrisisSupportHandler
import com.mindcluster.safediary.assistantai.application.commands.OpenConversationCommand
import com.mindcluster.safediary.assistantai.application.commands.OpenConversationHandler
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isTyping: Boolean = false,
    val replyFailed: Boolean = false,
    val crisisResources: List<CrisisResource> = emptyList(),
    val conversations: List<ConversationSummary> = emptyList(),
    val isHistoryLoading: Boolean = false,
    val historyFailed: Boolean = false,
    val activeConversationId: String? = null
)

class AssistantViewModel(
    private val sendPromptHandler: SendPromptHandler,
    private val retryLastPromptHandler: RetryLastPromptHandler,
    private val getChatHistoryHandler: GetChatHistoryHandler,
    private val startNewChatHandler: StartNewChatHandler,
    private val getCrisisSupportHandler: GetCrisisSupportHandler,
    private val dismissCrisisSupportHandler: DismissCrisisSupportHandler,
    private val getConversationListHandler: GetConversationListHandler,
    private val openConversationHandler: OpenConversationHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getChatHistoryHandler.handle().collect { list ->
                _uiState.update { it.copy(messages = list) }
            }
        }
        viewModelScope.launch {
            getCrisisSupportHandler.handle().collect { resources ->
                _uiState.update { it.copy(crisisResources = resources) }
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
        _uiState.update { it.copy(isTyping = true, replyFailed = false, input = "") }
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

    fun onInputChanged(newInput: String) {
        _uiState.update { it.copy(input = newInput) }
    }

    fun send() {
        val prompt = _uiState.value.input.trim()
        if (prompt.isBlank() || _uiState.value.isTyping) return

        _uiState.update { it.copy(input = "", isTyping = true, replyFailed = false) }
        viewModelScope.launch {
            val result = sendPromptHandler.handle(SendPromptCommand(prompt))
            _uiState.update {
                it.copy(
                    isTyping = false,
                    replyFailed = result.isFailure,
                    activeConversationId = result.getOrNull() ?: it.activeConversationId
                )
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
        viewModelScope.launch {
            startNewChatHandler.handle()
            _uiState.update { it.copy(input = "", isTyping = false, replyFailed = false, activeConversationId = null) }
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
        private val openConversationHandler: OpenConversationHandler
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
                openConversationHandler
            ) as T
        }
    }
}
