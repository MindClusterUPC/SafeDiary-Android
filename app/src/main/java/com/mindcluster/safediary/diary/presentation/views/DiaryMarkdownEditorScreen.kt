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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Visibility
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindcluster.safediary.R
import com.mindcluster.safediary.diary.presentation.components.MarkdownFormattingToolbar
import com.mindcluster.safediary.diary.presentation.components.MarkdownRenderView
import com.mindcluster.safediary.diary.presentation.viewmodels.DiaryViewModel
import com.mindcluster.safediary.shared.presentation.theme.BackgroundSanctuary
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal
import com.mindcluster.safediary.shared.presentation.theme.SlatePlaceholder
import com.mindcluster.safediary.shared.presentation.theme.SlateSubtle

private enum class EditorTab {
    EDIT,
    PREVIEW
}

@Composable
fun DiaryMarkdownEditorScreen(
    viewModel: DiaryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val draft by viewModel.currentDraft.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(EditorTab.EDIT) }
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
    val currentContent = draft?.content.orEmpty()

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

                    // Mode Toggle: [ Edit | Preview ]
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFEDEFEF))
                            .padding(2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            EditorTabPill(
                                label = stringResource(R.string.tab_edit),
                                icon = Icons.Outlined.Edit,
                                isSelected = selectedTab == EditorTab.EDIT,
                                onClick = { selectedTab = EditorTab.EDIT }
                            )
                            EditorTabPill(
                                label = stringResource(R.string.tab_preview),
                                icon = Icons.Outlined.Visibility,
                                isSelected = selectedTab == EditorTab.PREVIEW,
                                onClick = { selectedTab = EditorTab.PREVIEW }
                            )
                        }
                    }

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
            if (selectedTab == EditorTab.EDIT) {
                MarkdownFormattingToolbar(
                    onInsertSymbol = { prefix, suffix ->
                        val updated = if (currentContent.isEmpty()) {
                            "$prefix$suffix"
                        } else {
                            "$currentContent\n$prefix$suffix"
                        }
                        viewModel.onContentChanged(updated)
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                EditorTab.EDIT -> {
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
                        // Title Input Field
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

                        // Markdown Body Input Field
                        BasicTextField(
                            value = currentContent,
                            onValueChange = { viewModel.onContentChanged(it) },
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
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
                                    if (currentContent.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.hint_note_content),
                                            fontSize = 16.sp,
                                            lineHeight = 24.sp,
                                            color = SlatePlaceholder
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                EditorTab.PREVIEW -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (currentTitle.isBlank()) stringResource(R.string.untitled_note) else currentTitle,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        MarkdownRenderView(markdown = currentContent, modifier = Modifier.weight(1f))
                    }
                }
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

@Composable
private fun EditorTabPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) Color.White else Color.Transparent,
        contentColor = if (isSelected) PrimaryNavy else SlateSubtle
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) SecondaryTeal else SlateSubtle,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}
