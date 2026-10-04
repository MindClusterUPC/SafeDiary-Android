package com.mindcluster.safediary.profiles.presentation.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindcluster.safediary.R
import com.mindcluster.safediary.profiles.presentation.components.LanguageBottomSheet
import com.mindcluster.safediary.profiles.presentation.components.SettingsItem
import com.mindcluster.safediary.profiles.presentation.components.SettingsSection
import com.mindcluster.safediary.profiles.presentation.components.SettingsSwitchItem
import com.mindcluster.safediary.profiles.presentation.viewmodels.SettingsViewModel
import com.mindcluster.safediary.shared.infrastructure.locale.AppLocaleManager
import com.mindcluster.safediary.shared.presentation.components.SafeDiaryCard
import com.mindcluster.safediary.shared.presentation.components.SafeDiaryTopBar
import com.mindcluster.safediary.shared.presentation.theme.BackgroundSanctuary
import com.mindcluster.safediary.shared.presentation.theme.OnSurfaceVariant
import com.mindcluster.safediary.shared.presentation.theme.PrimaryNavy
import com.mindcluster.safediary.shared.presentation.theme.SecondaryContainer
import com.mindcluster.safediary.shared.presentation.theme.SecondaryTeal

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
    onLanguageClick: () -> Unit = { viewModel.openLanguageSheet() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currentLanguage = remember(uiState) { AppLocaleManager.current(context) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundSanctuary,
        topBar = {
            SafeDiaryTopBar(
                sectionTitle = stringResource(R.string.shared_topbar_section_settings),
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Anonymous Profile Card
            SafeDiaryCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 16.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(SecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.shared_cd_user_avatar),
                            tint = PrimaryNavy,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_anonymous_alias),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryNavy
                        )
                        Text(
                            text = stringResource(R.string.settings_anonymous_id),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(9999.dp))
                                .background(SecondaryTeal.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = SecondaryTeal,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.settings_vault_badge),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = SecondaryTeal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Account & Privacy Section
            SettingsSection(title = stringResource(R.string.settings_section_account)) {
                SettingsItem(
                    title = stringResource(R.string.settings_item_edit_profile),
                    icon = Icons.Default.Edit,
                    onClick = {}
                )
                SettingsItem(
                    title = stringResource(R.string.settings_item_privacy_permissions),
                    icon = Icons.Default.Security,
                    showDivider = false,
                    onClick = {}
                )
            }

            // 2. Preferences Section
            SettingsSection(title = stringResource(R.string.settings_section_preferences)) {
                SettingsItem(
                    title = stringResource(R.string.settings_item_language),
                    icon = Icons.Default.Language,
                    trailingText = currentLanguage.displayName,
                    onClick = onLanguageClick
                )
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_item_notifications),
                    icon = Icons.Default.Notifications,
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications(it) }
                )
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_item_dark_theme),
                    icon = Icons.Default.DarkMode,
                    checked = uiState.darkThemeEnabled,
                    onCheckedChange = { viewModel.toggleDarkTheme(it) }
                )
                SettingsItem(
                    title = stringResource(R.string.settings_item_daily_reminder),
                    icon = Icons.Default.Schedule,
                    trailingText = stringResource(R.string.settings_daily_reminder_time),
                    showDivider = false,
                    onClick = {}
                )
            }

            // 3. Security Section
            SettingsSection(title = stringResource(R.string.settings_section_security)) {
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_item_pin_lock),
                    icon = Icons.Default.Lock,
                    checked = uiState.pinLockEnabled,
                    onCheckedChange = { viewModel.togglePinLock(it) }
                )
                SettingsSwitchItem(
                    title = stringResource(R.string.settings_item_biometric_lock),
                    icon = Icons.Default.Fingerprint,
                    checked = uiState.biometricEnabled,
                    onCheckedChange = { viewModel.toggleBiometric(it) },
                    showDivider = false
                )
            }

            // 4. Support & About Section
            SettingsSection(title = stringResource(R.string.settings_section_support)) {
                SettingsItem(
                    title = stringResource(R.string.settings_item_help_center),
                    icon = Icons.AutoMirrored.Filled.Help,
                    onClick = {}
                )
                SettingsItem(
                    title = stringResource(R.string.settings_item_terms),
                    icon = Icons.Default.Description,
                    onClick = {}
                )
                SettingsItem(
                    title = stringResource(R.string.settings_item_privacy_policy),
                    icon = Icons.Default.Policy,
                    onClick = {}
                )
                SettingsItem(
                    title = stringResource(R.string.settings_item_about),
                    subtitle = stringResource(R.string.settings_app_version),
                    icon = Icons.Default.Info,
                    showDivider = false,
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(9999.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_button_logout),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_dialog_logout_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryNavy
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_dialog_logout_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = stringResource(R.string.settings_button_logout),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = stringResource(R.string.shared_button_cancel),
                        color = OnSurfaceVariant
                    )
                }
            },
            containerColor = BackgroundSanctuary,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (uiState.isLanguageSheetOpen) {
        LanguageBottomSheet(
            currentLanguage = currentLanguage,
            onLanguageSelected = { selectedLang ->
                AppLocaleManager.set(context, selectedLang)
                viewModel.closeLanguageSheet()
            },
            onDismissRequest = {
                viewModel.closeLanguageSheet()
            }
        )
    }
}
