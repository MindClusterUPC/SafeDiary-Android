package com.mindcluster.safediary.diary.presentation.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindcluster.safediary.R
import com.mindcluster.safediary.diary.presentation.components.DiaryMarkup
import com.mindcluster.safediary.diary.domain.model.DiaryEntryAggregate
import com.mindcluster.safediary.diary.presentation.viewmodels.DiaryViewModel
import com.mindcluster.safediary.shared.presentation.theme.BackgroundSanctuary
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal
import com.mindcluster.safediary.shared.presentation.theme.SlatePlaceholder
import com.mindcluster.safediary.shared.presentation.theme.SlateSubtle
import com.mindcluster.safediary.shared.presentation.theme.SurfaceContainerLowest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiaryGalleryScreen(
    viewModel: DiaryViewModel,
    onOpenEntry: (localId: String) -> Unit,
    onCreateNewEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    var entryToDelete by remember { mutableStateOf<DiaryEntryAggregate?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        if (entries.isEmpty()) {
            EmptyGalleryState(
                onCreateNew = onCreateNewEntry,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(entries, key = { it.localId }) { entry ->
                    DiarySquareCard(
                        entry = entry,
                        onClick = { onOpenEntry(entry.localId) },
                        onDeleteClick = { entryToDelete = entry }
                    )
                }
            }
        }

        // Floating Action Button (+)
        FloatingActionButton(
            onClick = onCreateNewEntry,
            containerColor = SecondaryTeal,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 8.dp)
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.cd_create_new_diary_note),
                modifier = Modifier.size(28.dp)
            )
        }

        // Delete Confirmation Dialog
        entryToDelete?.let { entry ->
            AlertDialog(
                onDismissRequest = { entryToDelete = null },
                title = { Text(stringResource(R.string.dialog_delete_note_title)) },
                text = { Text(stringResource(R.string.dialog_delete_note_message, entry.title)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteEntry(entry.localId)
                            entryToDelete = null
                        }
                    ) {
                        Text(stringResource(R.string.action_delete), color = Color(0xFFD32F2F))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { entryToDelete = null }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }
    }
}

@Composable
private fun DiarySquareCard(
    entry: DiaryEntryAggregate,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val formattedDate = remember(entry.updatedAt) {
        SimpleDateFormat("HH:mm · dd/MM", Locale.getDefault()).format(Date(entry.updatedAt))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Title + 3-dots Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.cd_note_options),
                            tint = SlatePlaceholder,
                            modifier = Modifier.size(18.dp)
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
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            // Preview snippet
            Text(
                text = if (entry.content.isBlank()) stringResource(R.string.note_empty_snippet) else DiaryMarkup.plainText(entry.content),
                style = MaterialTheme.typography.bodySmall,
                color = if (entry.content.isBlank()) SlatePlaceholder else SlateSubtle,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            )

            // Timestamp footer
            Text(
                text = formattedDate,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = SlatePlaceholder
            )
        }
    }
}

@Composable
private fun EmptyGalleryState(
    onCreateNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFE6F3F1),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.AutoStories,
                    contentDescription = null,
                    tint = SecondaryTeal,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Text(
            text = stringResource(R.string.empty_diary_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryNavy
        )

        Text(
            text = stringResource(R.string.empty_diary_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = SlateSubtle,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(9999.dp),
            color = SecondaryTeal,
            contentColor = Color.White,
            modifier = Modifier.clickable(onClick = onCreateNew)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.action_create_first_note),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
