package com.mindcluster.safediary.iam.presentation.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindcluster.safediary.R
import com.mindcluster.safediary.iam.infrastructure.IamModule
import com.mindcluster.safediary.iam.presentation.viewmodels.SignInViewModel
import com.mindcluster.safediary.shared.presentation.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    sessionExpired: Boolean = false
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val iamModule = remember { IamModule.getInstance(context) }
    val viewModel: SignInViewModel = viewModel(
        factory = SignInViewModel.Factory(context.applicationContext as android.app.Application, iamModule)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isImeVisible = WindowInsets.isImeVisible

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundSanctuary
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = if (isImeVisible) Alignment.TopCenter else Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = if (isImeVisible) 12.dp else 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SafeDiary Brand Logo & Title (collapses smoothly when keyboard is open so fields are never covered)
                AnimatedVisibility(
                    visible = !isImeVisible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.safediary_logo),
                            contentDescription = stringResource(R.string.shared_cd_logo),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(18.dp))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.shared_topbar_title_prefix),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryNavy
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.shared_welcome_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                if (sessionExpired) {
                    Surface(color = SurfaceContainerLow, shape = RoundedCornerShape(16.dp)) {
                        Text(
                            stringResource(R.string.auth_session_expired),
                            Modifier.padding(16.dp),
                            color = PrimaryNavy,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Auth Card Form (Sign In / Sign Up)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Mode Segmented Switch (Iniciar sesión | Crear cuenta)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(9999.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                AuthModeTab(
                                    text = stringResource(R.string.auth_tab_login),
                                    selected = !uiState.isSignUpMode,
                                    onClick = { viewModel.setSignUpMode(false) },
                                    modifier = Modifier.weight(1f)
                                )
                                AuthModeTab(
                                    text = stringResource(R.string.auth_tab_register),
                                    selected = uiState.isSignUpMode,
                                    onClick = { viewModel.setSignUpMode(true) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Text(
                            text = stringResource(
                                if (uiState.isSignUpMode) R.string.auth_register_title else R.string.auth_login_title
                            ),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                        )

                        // Patient registration fields
                        if (uiState.isSignUpMode) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = stringResource(R.string.auth_full_name_label),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = uiState.fullName,
                                    onValueChange = { viewModel.onFullNameChanged(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(stringResource(R.string.auth_full_name_placeholder))
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Person,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B)
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words,
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SecondaryTeal,
                                        unfocusedBorderColor = Color(0xFFE2E8F0)
                                    )
                                )
                            }
                        }

                        // Email Field
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.auth_email_label),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = OnSurfaceVariant
                            )
                            OutlinedTextField(
                                value = uiState.email,
                                onValueChange = { viewModel.onEmailChanged(it) },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Mail,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B)
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SecondaryTeal,
                                    unfocusedBorderColor = Color(0xFFE2E8F0)
                                )
                            )
                        }

                        // Password Field
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.auth_password_label),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = OnSurfaceVariant
                            )
                            OutlinedTextField(
                                value = uiState.password,
                                onValueChange = { viewModel.onPasswordChanged(it) },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B)
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                                        Icon(
                                            imageVector = if (uiState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = stringResource(R.string.auth_toggle_password),
                                            tint = Color(0xFF64748B)
                                        )
                                    }
                                },
                                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = if (uiState.isSignUpMode) ImeAction.Next else ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        viewModel.submit(onSuccess = onSignInSuccess)
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SecondaryTeal,
                                    unfocusedBorderColor = Color(0xFFE2E8F0)
                                )
                            )
                            if (uiState.isSignUpMode) {
                                Text(
                                    text = stringResource(R.string.auth_password_hint),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = OnSurfaceVariant,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }

                        // Confirm Password Field (Sign Up Mode)
                        if (uiState.isSignUpMode) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = stringResource(R.string.auth_confirm_password_label),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (!uiState.passwordsMatch) ErrorRed else OnSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = uiState.confirmPassword,
                                    onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = !uiState.passwordsMatch,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Lock,
                                            contentDescription = null,
                                            tint = if (!uiState.passwordsMatch) ErrorRed else Color(0xFF64748B)
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { viewModel.toggleConfirmPasswordVisibility() }) {
                                            Icon(
                                                imageVector = if (uiState.isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = stringResource(R.string.auth_toggle_password),
                                                tint = Color(0xFF64748B)
                                            )
                                        }
                                    },
                                    visualTransformation = if (uiState.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            viewModel.submit(onSuccess = onSignInSuccess)
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (!uiState.passwordsMatch) ErrorRed else SecondaryTeal,
                                        unfocusedBorderColor = if (!uiState.passwordsMatch) ErrorRed else Color(0xFFE2E8F0)
                                    )
                                )
                                if (!uiState.passwordsMatch) {
                                    Text(
                                        text = stringResource(R.string.auth_error_passwords_mismatch),
                                        style = MaterialTheme.typography.bodySmall.copy(color = ErrorRed),
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }

                        // Error Message if any
                        if (uiState.errorMessage != null) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(color = ErrorRed),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.submit(onSuccess = onSignInSuccess)
                            },
                            enabled = uiState.canSubmit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryNavy,
                                disabledContainerColor = Color(0xFFCBD5E1),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(9999.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = stringResource(
                                        if (uiState.isSignUpMode) R.string.auth_register_button else R.string.auth_login_button
                                    ),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Bottom Mode Toggle Link
                        Text(
                            text = stringResource(
                                if (uiState.isSignUpMode) R.string.auth_switch_to_login else R.string.auth_switch_to_register
                            ),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = SecondaryTeal
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.toggleAuthMode() }
                                .padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthModeTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(9999.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(9999.dp),
        color = if (selected) PrimaryNavy else Color.Transparent
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White else OnSurfaceVariant
                )
            )
        }
    }
}
