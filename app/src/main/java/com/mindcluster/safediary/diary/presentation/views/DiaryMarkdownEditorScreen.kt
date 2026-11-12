package com.mindcluster.safediary.diary.presentation.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindcluster.safediary.R
import com.mindcluster.safediary.diary.presentation.components.DiaryFormattingToolbar
import com.mindcluster.safediary.diary.presentation.components.DiaryMarkup
import com.mindcluster.safediary.diary.presentation.viewmodels.DiaryViewModel
import com.mindcluster.safediary.shared.presentation.theme.BackgroundSanctuary
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryContainer
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal
import com.mindcluster.safediary.shared.presentation.theme.SlatePlaceholder
import com.mindcluster.safediary.shared.presentation.theme.SlateSubtle

@Composable
fun DiaryMarkdownEditorScreen(
    viewModel: DiaryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val draft by viewModel.currentDraft.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val contentFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (viewModel.currentDraft.value == null) {
            viewModel.openNewDraft()
        }
    }

    // Auto-save on exit/back navigation
    val onBack: () -> Unit = {
        viewModel.closeDraft()
        onNavigateBack()
    }

    BackHandler(onBack = onBack)

    DisposableEffect(Unit) {
        onDispose {
            viewModel.persistCurrentDraft()
        }
    }

    val currentTitle = draft?.title.orEmpty()

    // The body keeps its cursor and selection locally so formatting is applied where the writer is.
    var content by remember { mutableStateOf(TextFieldValue(draft?.content.orEmpty())) }
    LaunchedEffect(draft?.localId) {
        val stored = draft?.content.orEmpty()
        if (stored != content.text) content = TextFieldValue(stored, TextRange(stored.length))
    }
    val updateContent: (TextFieldValue) -> Unit = { updated ->
        content = updated
        viewModel.onContentChanged(updated.text)
    }
    val formatting = remember {
        DiaryMarkup.visualTransformation(SecondaryTeal, SecondaryContainer.copy(alpha = 0.35f))
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding(),
        containerColor = BackgroundSanctuary,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                            tint = PrimaryNavy
                        )
                    }

                    Text(
                        text = stringResource(R.string.diary_editor_heading),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryNavy
                    )

                    // Options Menu (⋮)
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.cd_note_options),
                                tint = PrimaryNavy
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_delete_note)) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFD32F2F))
                                },
                                onClick = {
                                    menuExpanded = false
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            DiaryFormattingToolbar(
                activeLineStyle = DiaryMarkup.lineStyleAtCursor(content),
                onLineStyle = { style ->
                    updateContent(DiaryMarkup.toggleLineStyle(content, style))
                    contentFocusRequester.requestFocus()
                },
                onInlineStyle = { style ->
                    updateContent(DiaryMarkup.toggleInlineStyle(content, style))
                    contentFocusRequester.requestFocus()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        contentFocusRequester.requestFocus()
                    }
            ) {
                BasicTextField(
                    value = currentTitle,
                    onValueChange = { viewModel.onTitleChanged(it) },
                    textStyle = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    ),
                    cursorBrush = SolidColor(SecondaryTeal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (currentTitle.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.hint_note_title),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlatePlaceholder
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (content.text.isEmpty()) {
                    WritingPrompts(
                        onPromptSelected = { prompt ->
                            val text = "> $prompt\n"
                            updateContent(TextFieldValue(text, TextRange(text.length)))
                            contentFocusRequester.requestFocus()
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                BasicTextField(
                    value = content,
                    onValueChange = updateContent,
                    visualTransformation = formatting,
                    textStyle = TextStyle(
                        fontSize = 17.sp,
                        lineHeight = 26.sp,
                        color = PrimaryNavy
                    ),
                    cursorBrush = SolidColor(SecondaryTeal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .focusRequester(contentFocusRequester),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.TopStart
                        ) {
                            if (content.text.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.hint_note_content),
                                    fontSize = 17.sp,
                                    lineHeight = 26.sp,
                                    color = SlatePlaceholder
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text(stringResource(R.string.dialog_delete_note_title)) },
                text = { Text(stringResource(R.string.dialog_delete_note_prompt)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirm = false
                            draft?.localId?.let { viewModel.deleteEntry(it) }
                            onNavigateBack()
                        }
                    ) {
                        Text(stringResource(R.string.action_delete), color = Color(0xFFD32F2F))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }
    }
}

/** Gentle questions to start writing when the page is blank, as in a guided journal. */
@Composable
private fun WritingPrompts(onPromptSelected: (String) -> Unit) {
    val prompts = listOf(
        stringResource(R.string.diary_prompt_today),
        stringResource(R.string.diary_prompt_feeling),
        stringResource(R.string.diary_prompt_grateful),
        stringResource(R.string.diary_prompt_release)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        prompts.forEach { prompt ->
            Surface(
                onClick = { onPromptSelected(prompt) },
                shape = CircleShape,
                color = SecondaryContainer.copy(alpha = 0.35f),
                contentColor = PrimaryNavy
            ) {
                Text(
                    text = prompt,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}
