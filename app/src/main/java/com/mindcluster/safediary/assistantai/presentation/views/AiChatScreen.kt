package com.mindcluster.safediary.assistantai.presentation.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindcluster.safediary.R
import com.mindcluster.safediary.assistantai.presentation.components.ChatEmptyState
import com.mindcluster.safediary.assistantai.presentation.components.ChatInputBar
import com.mindcluster.safediary.assistantai.presentation.components.MessageBubble
import com.mindcluster.safediary.assistantai.presentation.components.SuggestionChips
import com.mindcluster.safediary.assistantai.presentation.components.TypingIndicator
import com.mindcluster.safediary.assistantai.presentation.viewmodels.AssistantViewModel
import com.mindcluster.safediary.shared.presentation.components.SafeDiaryTopBar
import com.mindcluster.safediary.shared.presentation.theme.BackgroundSanctuary
import com.mindcluster.safediary.shared.presentation.theme.OnSurfaceVariant
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy

@Composable
fun AiChatScreen(
    viewModel: AssistantViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val suggestions = listOf(
        stringResource(R.string.chat_suggestion_1),
        stringResource(R.string.chat_suggestion_2),
        stringResource(R.string.chat_suggestion_3),
        stringResource(R.string.chat_suggestion_4)
    )

    LaunchedEffect(uiState.messages.size, uiState.isTyping) {
        if (uiState.messages.isNotEmpty()) {
            val totalCount = uiState.messages.size + (if (uiState.isTyping) 1 else 0)
            listState.animateScrollToItem(totalCount - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundSanctuary,
        topBar = {
            SafeDiaryTopBar(
                sectionTitle = stringResource(R.string.shared_topbar_section_assistant),
                actions = {
                    IconButton(onClick = { viewModel.newChat() }) {
                        Icon(
                            imageVector = Icons.Default.AddComment,
                            contentDescription = stringResource(R.string.chat_cd_new_chat),
                            tint = PrimaryNavy
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.shared_cd_settings),
                            tint = PrimaryNavy
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.messages.isEmpty()) {
                    ChatEmptyState(
                        suggestions = suggestions,
                        onSuggestionSelected = { suggestion ->
                            viewModel.onSuggestionSelected(suggestion)
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            MessageBubble(message = message)
                        }
                        if (uiState.isTyping) {
                            item(key = "typing_indicator") {
                                TypingIndicator()
                            }
                        }
                    }
                }
            }

            if (uiState.messages.isNotEmpty() && !uiState.isTyping) {
                SuggestionChips(
                    suggestions = suggestions,
                    onSuggestionSelected = { suggestion ->
                        viewModel.onSuggestionSelected(suggestion)
                    }
                )
            }

            Text(
                text = stringResource(R.string.chat_disclaimer),
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 2.dp)
            )

            ChatInputBar(
                value = uiState.input,
                onValueChange = { viewModel.onInputChanged(it) },
                onSend = { viewModel.send() },
                isTyping = uiState.isTyping
            )
        }
    }
}
