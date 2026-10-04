package com.mindcluster.safediary.assistantai.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mindcluster.safediary.assistantai.application.commands.SendPromptCommand
import com.mindcluster.safediary.assistantai.application.commands.SendPromptHandler
import com.mindcluster.safediary.assistantai.application.queries.GetChatHistoryHandler
import com.mindcluster.safediary.assistantai.domain.model.ChatMessage
import com.mindcluster.safediary.assistantai.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isTyping: Boolean = false,
    val errorMessage: String? = null
)

class AssistantViewModel(
    private val sendPromptHandler: SendPromptHandler,
    private val getChatHistoryHandler: GetChatHistoryHandler,
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getChatHistoryHandler.handle().collect { list ->
                _uiState.update { it.copy(messages = list) }
            }
        }
    }

    fun onInputChanged(newInput: String) {
        _uiState.update { it.copy(input = newInput) }
    }

    fun send() {
        val prompt = _uiState.value.input.trim()
        if (prompt.isBlank() || _uiState.value.isTyping) return

        _uiState.update { it.copy(input = "", isTyping = true, errorMessage = null) }
        viewModelScope.launch {
            val result = sendPromptHandler.handle(SendPromptCommand(prompt))
            _uiState.update {
                it.copy(
                    isTyping = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage
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
            conversationRepository.reset()
            _uiState.update { it.copy(input = "", isTyping = false, errorMessage = null) }
        }
    }

    class Factory(
        private val sendPromptHandler: SendPromptHandler,
        private val getChatHistoryHandler: GetChatHistoryHandler,
        private val conversationRepository: ConversationRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AssistantViewModel(
                sendPromptHandler,
                getChatHistoryHandler,
                conversationRepository
            ) as T
        }
    }
}
