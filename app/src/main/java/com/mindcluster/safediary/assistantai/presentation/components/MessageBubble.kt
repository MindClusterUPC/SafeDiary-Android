package com.mindcluster.safediary.assistantai.presentation.components

import android.content.ClipData
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mindcluster.safediary.R
import com.mindcluster.safediary.assistantai.domain.model.ChatMessage
import com.mindcluster.safediary.assistantai.domain.model.MessageAuthor
import com.mindcluster.safediary.shared.presentation.theme.DarkText
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal
import com.mindcluster.safediary.shared.presentation.theme.SlateBorder
import com.mindcluster.safediary.shared.presentation.theme.SlateSubtle

@Composable
fun MessageBubble(
    message: ChatMessage,
    isLastAssistantReply: Boolean = false,
    isTyping: Boolean = false,
    onEditMessage: ((ChatMessage) -> Unit)? = null,
    onRegenerate: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isUser = message.author == MessageAuthor.USER

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // Assistant Avatar: 30dp circle #006B5F with a white ✦ (AutoAwesome, 16dp)
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(SecondaryTeal),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Surface(
                shape = if (isUser) {
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 4.dp
                    )
                } else {
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 18.dp
                    )
                },
                color = if (isUser) PrimaryNavy else Color.White,
                border = if (isUser) null else BorderStroke(1.dp, SlateBorder),
                shadowElevation = if (isUser) 1.dp else 0.dp,
                modifier = Modifier.widthIn(max = 296.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isUser) Color.White else DarkText
                        )
                    )
                }
            }

            if (isUser) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 2.dp, end = 2.dp)
                ) {
                    CopyButton(
                        text = message.content,
                        contentDesc = stringResource(R.string.chat_cd_copy_user)
                    )
                    if (message.remoteId != null && !isTyping && onEditMessage != null) {
                        IconButton(
                            onClick = { onEditMessage(message) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.chat_cd_edit_message),
                                tint = SlateSubtle,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 2.dp, start = 2.dp)
                ) {
                    CopyButton(
                        text = message.content,
                        contentDesc = stringResource(R.string.chat_cd_copy)
                    )
                    if (isLastAssistantReply && message.remoteId != null && !isTyping && onRegenerate != null) {
                        IconButton(
                            onClick = { onRegenerate() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.chat_cd_regenerate_reply),
                                tint = SlateSubtle,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CopyButton(
    text: String,
    contentDesc: String
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(1500)
            copied = false
        }
    }

    IconButton(
        onClick = {
            scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("SafeDiary", text)))
                copied = true
            }
        },
        modifier = Modifier.size(28.dp)
    ) {
        Icon(
            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
            contentDescription = contentDesc,
            tint = SlateSubtle,
            modifier = Modifier.size(15.dp)
        )
    }
}
