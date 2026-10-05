package com.mindcluster.safediary.profiles.presentation.views

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindcluster.safediary.BuildConfig
import com.mindcluster.safediary.R
import com.mindcluster.safediary.assistantai.domain.model.DiaritoPersonality
import com.mindcluster.safediary.profiles.presentation.components.LanguageBottomSheet
import com.mindcluster.safediary.profiles.presentation.viewmodels.SettingsViewModel
import com.mindcluster.safediary.shared.infrastructure.locale.AppLocaleManager
import com.mindcluster.safediary.shared.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currentLanguage = remember(uiState) { AppLocaleManager.current(context) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundSanctuary,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryNavy
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.shared_cd_back),
                            tint = PrimaryNavy
                        )
                    }
                },
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundSanctuary)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Diarito Personality Selector
            SettingsSection(title = stringResource(R.string.settings_section_diarito)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerLowest,
                    shadowElevation = 1.dp
                ) {
                    Column {
                        PERSONALITY_OPTIONS.forEachIndexed { index, item ->
                            PersonalityRow(
                                option = item,
                                isSelected = uiState.selectedToneIndex == index,
                                onClick = { viewModel.selectTone(index) }
                            )
                            if (index < PERSONALITY_OPTIONS.lastIndex) {
                                HorizontalDivider(
                                    color = SurfaceContainerLow,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. Preferences (Language)
            SettingsSection(title = stringResource(R.string.settings_section_preferences)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerLowest,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openLanguageSheet() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = PrimaryNavy,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = stringResource(R.string.settings_item_language),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = OnSurface
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentLanguage.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = OutlineBorder,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 3. Immediate Help (113 Dialer)
            SettingsSection(title = stringResource(R.string.settings_section_help)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = ErrorContainer.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.settings_help_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Button(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:113")))
                                } catch (_: ActivityNotFoundException) {
                                    // No dialer available on this device.
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryNavy,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(9999.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.settings_crisis_call_btn),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = OutlineBorder,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            )
        }
    }

    if (uiState.isLanguageSheetOpen) {
        LanguageBottomSheet(
            currentLanguage = currentLanguage,
            onLanguageSelected = { selectedLang ->
                AppLocaleManager.set(context, selectedLang)
                viewModel.closeLanguageSheet()
            },
            onDismissRequest = { viewModel.closeLanguageSheet() }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = OnSurfaceVariant
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        content()
    }
}

@Composable
private fun PersonalityRow(
    option: PersonalityOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isSelected) SecondaryTeal else SurfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else PrimaryNavy,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(option.nameRes),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(option.descriptionRes),
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = SecondaryTeal,
                unselectedColor = OutlineBorder
            )
        )
    }
}

private data class PersonalityOption(
    val personality: DiaritoPersonality,
    val nameRes: Int,
    val descriptionRes: Int,
    val icon: ImageVector
)

private val PERSONALITY_OPTIONS = listOf(
    PersonalityOption(DiaritoPersonality.SOL, R.string.personality_sol_name, R.string.settings_personality_sol_desc, Icons.Default.WbSunny),
    PersonalityOption(DiaritoPersonality.LUMA, R.string.personality_luma_name, R.string.settings_personality_luma_desc, Icons.Default.Lightbulb),
    PersonalityOption(DiaritoPersonality.KAI, R.string.personality_kai_name, R.string.settings_personality_kai_desc, Icons.Default.TrackChanges),
    PersonalityOption(DiaritoPersonality.NARA, R.string.personality_nara_name, R.string.settings_personality_nara_desc, Icons.Default.SelfImprovement)
)
