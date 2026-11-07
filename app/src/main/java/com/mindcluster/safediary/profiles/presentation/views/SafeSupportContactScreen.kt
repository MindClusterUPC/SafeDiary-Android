package com.mindcluster.safediary.profiles.presentation.views

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindcluster.safediary.R
import com.mindcluster.safediary.profiles.domain.model.RelationshipType
import com.mindcluster.safediary.profiles.infrastructure.ProfilesModule
import com.mindcluster.safediary.profiles.presentation.viewmodels.SafeContactViewModel
import com.mindcluster.safediary.shared.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeSupportContactScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val profilesModule = remember { ProfilesModule.getInstance(context) }
    val viewModel: SafeContactViewModel = viewModel(
        factory = SafeContactViewModel.Factory(context.applicationContext as android.app.Application, profilesModule)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRelDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundSanctuary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.safe_contact_title),
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
                    Text(
                        text = stringResource(R.string.shared_button_cancel),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = OnSurfaceVariant,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable(onClick = onNavigateBack)
                    )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Emotional Safety Net Blue Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFE0E7FF)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = PrimaryNavy,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.safe_contact_net_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.safe_contact_net_desc),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PrimaryNavy.copy(alpha = 0.85f),
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            // Contact Information Form
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = SurfaceContainerLowest,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.safe_contact_info),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryNavy
                    )

                    // Full Name
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.safe_contact_name),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnSurfaceVariant
                        )
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.onNameChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Carlos Vance") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B)
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SecondaryTeal,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }

                    // Relationship Dropdown
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.safe_contact_rel),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnSurfaceVariant
                        )
                        Box {
                            OutlinedTextField(
                                value = when (uiState.relationship) {
                                    RelationshipType.PARENT -> stringResource(R.string.relationship_parent)
                                    RelationshipType.SPOUSE -> stringResource(R.string.relationship_spouse)
                                    RelationshipType.PARTNER -> stringResource(R.string.relationship_partner)
                                    RelationshipType.SIBLING -> stringResource(R.string.relationship_sibling)
                                    RelationshipType.CHILD -> stringResource(R.string.relationship_child)
                                    RelationshipType.FRIEND -> stringResource(R.string.relationship_friend)
                                    RelationshipType.GUARDIAN -> stringResource(R.string.relationship_guardian)
                                    RelationshipType.DOCTOR -> stringResource(R.string.relationship_doctor)
                                    RelationshipType.OTHER -> stringResource(R.string.relationship_other)
                                },
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isRelDropdownExpanded = true },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.clickable { isRelDropdownExpanded = true }
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SecondaryTeal,
                                    unfocusedBorderColor = Color(0xFFE2E8F0)
                                )
                            )
                            DropdownMenu(
                                expanded = isRelDropdownExpanded,
                                onDismissRequest = { isRelDropdownExpanded = false }
                            ) {
                                RelationshipType.entries.forEach { rel ->
                                    val label = when (rel) {
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
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                             viewModel.onRelationshipChanged(rel)
                                            isRelDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Phone Number
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.safe_contact_phone),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnSurfaceVariant
                        )
                        OutlinedTextField(
                            value = uiState.phoneNumber,
                            onValueChange = { viewModel.onPhoneChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("+1 (555) 234-8910") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B)
                                )
                            },
                            trailingIcon = {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFCCFBF1),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.safe_contact_sms_ready),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F766E),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SecondaryTeal,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }

                    // Emergency Email
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.safe_contact_email),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = OnSurfaceVariant
                        )
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.onEmailChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("carlos.vance@familycare.org") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Mail,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SecondaryTeal,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Notify on Extended Inactivity Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.safe_contact_notify_inactivity),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryNavy
                            )
                            Text(
                                text = stringResource(R.string.safe_contact_notify_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.notifyOnInactivity,
                            onCheckedChange = { viewModel.onNotifyChanged(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SecondaryTeal,
                                uncheckedThumbColor = OnSurfaceVariant,
                                uncheckedTrackColor = Color(0xFFE2E8F0)
                            )
                        )
                    }
                }
            }

            // Remove Safe Contact Card
            if (uiState.hasExistingContact) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFFFF5F5),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Outlined.WarningAmber,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.safe_contact_remove_title),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorRed
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.safe_contact_remove_desc),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF7F1D1D),
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.requestDeleteConfirmation() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDC2626),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(9999.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.safe_contact_remove_confirm),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Save Contact Changes Button
            Button(
                onClick = {
                    viewModel.saveContact(
                        onSuccess = {
                            Toast.makeText(context, context.getString(R.string.safe_contact_saved_toast), Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                    )
                },
                enabled = uiState.canSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNavy,
                    disabledContainerColor = Color(0xFFCBD5E1),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(9999.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = stringResource(R.string.safe_contact_save_btn),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Zero Knowledge Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.safe_contact_encrypted_foot),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = OnSurfaceVariant
                )
            }
        }
    }

    // Confirmation Dialog before deletion
    if (uiState.showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteConfirmation() },
            title = {
                Text(
                    text = stringResource(R.string.safe_contact_dialog_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryNavy
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.safe_contact_dialog_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeContact(
                            onSuccess = {
                                Toast.makeText(context, context.getString(R.string.safe_contact_removed_toast), Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White)
                ) {
                    Text(stringResource(R.string.safe_contact_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteConfirmation() }) {
                    Text(stringResource(R.string.shared_button_cancel), color = PrimaryNavy)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
