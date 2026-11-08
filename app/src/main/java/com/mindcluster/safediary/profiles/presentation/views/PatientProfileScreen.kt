package com.mindcluster.safediary.profiles.presentation.views

import android.widget.Toast
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindcluster.safediary.R
import com.mindcluster.safediary.payments.infrastructure.PaymentsModule
import com.mindcluster.safediary.profiles.domain.model.RelationshipType
import com.mindcluster.safediary.profiles.infrastructure.ProfilesModule
import com.mindcluster.safediary.profiles.presentation.components.AvatarChangeBottomSheet
import com.mindcluster.safediary.profiles.presentation.viewmodels.PatientProfileViewModel
import com.mindcluster.safediary.shared.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditEmail: () -> Unit,
    onNavigateToSafeContact: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profilesModule = remember { ProfilesModule.getInstance(context) }
    val paymentsModule = remember { PaymentsModule.getInstance(context) }

    val viewModel: PatientProfileViewModel = viewModel(
        factory = PatientProfileViewModel.Factory(context.applicationContext as android.app.Application, profilesModule)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile = uiState.profile

    val patientAccountId = profilesModule.authSessionPreferences.patientAccountId
    val defaultPaymentMethod by produceState<com.mindcluster.safediary.payments.domain.model.PaymentMethodCard?>(
        initialValue = null,
        key1 = profile?.id
    ) {
        val sub = paymentsModule.repository.getSubscription(patientAccountId)
        value = sub.paymentMethods.firstOrNull { it.isDefault } ?: sub.paymentMethods.firstOrNull()
    }

    var showEditNameDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundSanctuary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.shared_cd_settings),
                            tint = PrimaryNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundSanctuary)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header with Avatar and Name only (all badges and metrics removed as requested)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFB4F2E1))
                            .clickable { viewModel.openAvatarSheet() },
                        contentAlignment = Alignment.Center
                    ) {
                        val initials = profile?.initials?.ifBlank { "SD" } ?: "SD"
                        Text(
                            text = initials,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(PrimaryNavy)
                            .clickable { viewModel.openAvatarSheet() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = profile?.fullName?.ifBlank { "..." } ?: "...",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryNavy
                )
            }

            // Section 1: Personal Information
            SectionCard(title = stringResource(R.string.profile_section_personal)) {
                // Full name row with functioning edit dialog
                RowItem(
                    icon = Icons.Outlined.Person,
                    text = profile?.fullName?.ifBlank { "..." } ?: "...",
                    actionLabel = stringResource(R.string.profile_btn_edit),
                    onActionClick = {
                        showEditNameDialog = true
                    }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                // Email row
                RowItem(
                    icon = Icons.Outlined.Email,
                    text = profile?.email?.ifBlank { "..." } ?: "...",
                    badge = stringResource(R.string.profile_verified),
                    actionLabel = stringResource(R.string.profile_btn_change),
                    onActionClick = onNavigateToEditEmail
                )

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                // Emergency Contact row (shows configured contact or "Sin contacto configurado")
                val emergencyContactText = profile?.emergencyContact?.let { ec ->
                    val relLabel = when (ec.relationship) {
                        RelationshipType.PARENT -> stringResource(R.string.relationship_parent)
                        RelationshipType.SPOUSE -> stringResource(R.string.relationship_spouse)
                        RelationshipType.PARTNER -> stringResource(R.string.relationship_partner)
                        RelationshipType.SIBLING -> stringResource(R.string.relationship_sibling)
                        RelationshipType.CHILD -> stringResource(R.string.relationship_child)
                        RelationshipType.FRIEND -> stringResource(R.string.relationship_friend)
                        RelationshipType.GUARDIAN -> stringResource(R.string.relationship_guardian)
                        RelationshipType.DOCTOR -> stringResource(R.string.relationship_doctor)
                        RelationshipType.OTHER -> stringResource(R.string.relationship_other)
                    }
                    "${ec.name} ($relLabel)"
                } ?: stringResource(R.string.profile_no_emergency_contact)

                RowItem(
                    icon = Icons.Outlined.FavoriteBorder,
                    text = emergencyContactText,
                    actionLabel = stringResource(R.string.profile_btn_manage),
                    onActionClick = onNavigateToSafeContact
                )
            }

            // Section 2: Payment Methods & Digital Wallet (fully dynamic)
            SectionCard(title = stringResource(R.string.profile_payment_methods)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF0FDF4))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.CreditCard,
                                contentDescription = null,
                                tint = SecondaryTeal,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = defaultPaymentMethod?.let { "${it.brand} •••• ${it.last4}" }
                                    ?: stringResource(R.string.payments_title),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryNavy
                            )
                            if (defaultPaymentMethod?.isDefault == true) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF99F6E4)
                                ) {
                                    Text(
                                        text = stringResource(R.string.payments_default_badge),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F766E),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${stringResource(R.string.profile_btn_wallet)} >",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = SecondaryTeal,
                            modifier = Modifier.clickable { onNavigateToPayments() }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.profile_wallet_escrow),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = OnSurfaceVariant
                    )
                }
            }

            // Section 3: Security & Privacy (Daily Reminder removed as requested)
            SectionCard(title = stringResource(R.string.profile_security_privacy)) {
                // FaceID switch
                SwitchRow(
                    icon = Icons.Outlined.Fingerprint,
                    title = stringResource(R.string.profile_face_id),
                    subtitle = stringResource(R.string.profile_face_id_desc),
                    checked = profile?.biometricLockEnabled ?: true,
                    onCheckedChange = { viewModel.toggleBiometricLock(it) }
                )

                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                // Export encrypted journal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            Toast.makeText(context, "Exportando diario cifrado...", Toast.LENGTH_SHORT).show()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = null,
                            tint = PrimaryNavy,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.profile_export_journal),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = PrimaryNavy
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

            // Save Changes button
            Button(
                onClick = {
                    Toast.makeText(context, "Cambios guardados con éxito", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNavy,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_save_changes),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Log Out Securely
            Text(
                text = stringResource(R.string.profile_logout),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .clickable {
                        profilesModule.profilePreferences.clear()
                        profilesModule.authSessionPreferences.clear()
                        Toast.makeText(context, context.getString(R.string.profile_logged_out_success), Toast.LENGTH_SHORT).show()
                        onLogout()
                    }
            )
        }
    }

    // Personal Info Edit Dialog (First & Last Name)
    if (showEditNameDialog) {
        var firstNameInput by remember(profile) { mutableStateOf(profile?.firstName.orEmpty()) }
        var lastNameInput by remember(profile) { mutableStateOf(profile?.lastName.orEmpty()) }

        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.profile_edit_name_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryNavy
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = firstNameInput,
                        onValueChange = { firstNameInput = it },
                        label = { Text(stringResource(R.string.profile_first_name_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryTeal,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )
                    OutlinedTextField(
                        value = lastNameInput,
                        onValueChange = { lastNameInput = it },
                        label = { Text(stringResource(R.string.profile_last_name_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SecondaryTeal,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (firstNameInput.isNotBlank() && lastNameInput.isNotBlank()) {
                            viewModel.updateName(firstNameInput.trim(), lastNameInput.trim())
                            Toast.makeText(context, context.getString(R.string.profile_name_updated_toast), Toast.LENGTH_SHORT).show()
                            showEditNameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy, contentColor = Color.White)
                ) {
                    Text(stringResource(R.string.shared_button_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text(stringResource(R.string.shared_button_cancel), color = PrimaryNavy)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Avatar Bottom Sheet
    if (uiState.isAvatarSheetOpen) {
        AvatarChangeBottomSheet(
            currentInitials = profile?.initials?.ifBlank { "SD" } ?: "SD",
            onAvatarSelected = { viewModel.selectAvatar(it) },
            onDismissRequest = { viewModel.closeAvatarSheet() }
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = PrimaryNavy
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun RowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    badge: String? = null,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryNavy,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = OnSurface
            )
            if (badge != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFCCFBF1)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F766E),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = SecondaryTeal,
            modifier = Modifier.clickable(onClick = onActionClick)
        )
    }
}

@Composable
private fun SwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryNavy,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = OnSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SecondaryTeal,
                uncheckedThumbColor = OnSurfaceVariant,
                uncheckedTrackColor = Color(0xFFE2E8F0)
            )
        )
    }
}
