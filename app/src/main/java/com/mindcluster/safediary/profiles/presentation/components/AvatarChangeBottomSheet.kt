package com.mindcluster.safediary.profiles.presentation.components

import androidx.compose.runtime.saveable.rememberSaveable
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionPreferences
import com.mindcluster.safediary.profiles.infrastructure.local.AvatarImageStorage
import com.mindcluster.safediary.shared.presentation.components.ProfileAvatarContent
import kotlinx.coroutines.CancellationException
import java.io.File
import androidx.core.content.FileProvider

import android.net.Uri
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindcluster.safediary.R
import com.mindcluster.safediary.profiles.domain.model.AvatarConfig
import com.mindcluster.safediary.shared.presentation.theme.*



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarChangeBottomSheet(
    currentInitials: String,
    onAvatarSelected: (AvatarConfig) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var uploadError by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var cameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val accountId = remember { AuthSessionPreferences(context).patientAccountId }
    fun importPhoto(uri: Uri, capture: Boolean = false) {
        isImporting = true
        uploadError = false
        scope.launch {
            try {
                val saved = withContext(Dispatchers.IO) {
                    AvatarImageStorage.importImage(context, uri, accountId)
                }
                check(AuthSessionPreferences(context).patientAccountId == accountId)
                onAvatarSelected(AvatarConfig.CustomImage(saved))
            } catch (e: CancellationException) { throw e
            } catch (_: Exception) { uploadError = true
            } finally {
                isImporting = false
                if (capture) { runCatching { context.contentResolver.delete(uri, null, null) }; cameraUri = null }
            }
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) importPhoto(uri)
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        cameraUri?.let { value ->
            val uri = Uri.parse(value)
            if (success) importPhoto(uri, capture = true)
            else { runCatching { context.contentResolver.delete(uri, null, null) }; cameraUri = null }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isImporting) onDismissRequest() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFCBD5E1))
            )
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Close X
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.avatar_sheet_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryNavy
                    )
                    Text(
                        text = stringResource(R.string.avatar_sheet_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismissRequest,
                    enabled = !isImporting,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.shared_button_cancel),
                        tint = SlateMenuIcon,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Big Avatar Circle
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB4F2E1)),
                contentAlignment = Alignment.Center
            ) {
                ProfileAvatarContent(Modifier.matchParentSize(), fontSize = 28.sp)
            }

            // Error banner if rejected
            if (uploadError) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFECEC),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.avatar_image_error),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF991B1B),
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            if (isImporting) {
                CircularProgressIndicator(Modifier.size(28.dp), color = SecondaryTeal)
                Text(stringResource(R.string.avatar_saving), color = PrimaryNavy)
            }
            // Action Items
            if (!isImporting) {
                AvatarActionItem(
                    icon = Icons.Default.PhotoCamera,
                    title = stringResource(R.string.avatar_take_photo),
                    subtitle = stringResource(R.string.avatar_take_photo_desc),
                    onClick = {
                        try {
                            val folder = File(context.cacheDir, "avatar-capture").apply { mkdirs() }
                            val file = File.createTempFile("capture-", ".jpg", folder)
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.avatar-files", file)
                            cameraUri = uri.toString()
                            cameraLauncher.launch(uri)
                        } catch (_: Exception) {
                        cameraUri?.let { value -> runCatching { context.contentResolver.delete(Uri.parse(value), null, null) } }
                        cameraUri = null
                        uploadError = true
                    }
                    }
                )

                AvatarActionItem(
                    icon = Icons.Default.Image,
                    title = stringResource(R.string.avatar_choose_gallery),
                    subtitle = stringResource(R.string.avatar_choose_gallery_desc),
                    onClick = { galleryLauncher.launch("image/*") }
                )

                AvatarActionItem(
                    icon = Icons.Default.AutoAwesome,
                    title = stringResource(R.string.avatar_choose_preset),
                    subtitle = stringResource(R.string.avatar_choose_preset_desc),
                    iconTint = SecondaryTeal,
                    onClick = {
                        onAvatarSelected(AvatarConfig.PresetAvatar("preset_serene", "Serene"))
                    }
                )

                // Remove photo item
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAvatarSelected(AvatarConfig.Initials(currentInitials))
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFF5F5),
                    border = BorderStroke(1.dp, Color(0xFFFFE2E2))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = stringResource(R.string.avatar_remove_photo),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = ErrorRed
                        )
                    }
                }

            }
            // Cancel Button
            Button(
                onClick = onDismissRequest,
                enabled = !isImporting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = PrimaryNavy
                ),
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    text = stringResource(R.string.shared_button_cancel),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AvatarActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconTint: Color = PrimaryNavy
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryNavy
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
