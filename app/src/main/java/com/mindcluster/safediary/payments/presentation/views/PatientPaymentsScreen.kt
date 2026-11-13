package com.mindcluster.safediary.payments.presentation.views

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mindcluster.safediary.R
import com.mindcluster.safediary.payments.domain.model.SubscriptionPlan
import com.mindcluster.safediary.payments.infrastructure.PaymentsModule
import com.mindcluster.safediary.payments.presentation.components.HealthWalletCard
import com.mindcluster.safediary.payments.presentation.components.PaymentHistoryFilterChips
import com.mindcluster.safediary.payments.presentation.components.PaymentHistoryList
import com.mindcluster.safediary.payments.presentation.components.StripeCheckoutWebViewDialog
import com.mindcluster.safediary.payments.presentation.viewmodels.PatientPaymentsViewModel
import com.mindcluster.safediary.shared.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientPaymentsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val paymentsModule = remember { PaymentsModule.getInstance(context) }
    val viewModel: PatientPaymentsViewModel = viewModel(
        factory = PatientPaymentsViewModel.Factory(context.applicationContext as android.app.Application, paymentsModule)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sub = uiState.subscription
    var confirmDowngradeToFree by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundSanctuary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.payments_title),
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
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Subscription Plan Section
            SubscriptionPlanSection(
                currentPlan = sub?.plan ?: SubscriptionPlan.FREE,
                activeUntil = sub?.activeUntil,
                isBusy = uiState.isUpdatingPlan || uiState.isSettingUpPaymentMethod,
                isUpdatingPlan = uiState.isUpdatingPlan,
                onSelectPaidPlan = { plan -> viewModel.startCheckout(plan) },
                onRequestDowngradeFree = { confirmDowngradeToFree = true }
            )

            // Saved Payment Methods Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.payments_saved_methods),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryNavy
                    )
                    Text(
                        text = stringResource(R.string.payments_add_method),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (uiState.isSettingUpPaymentMethod) OnSurfaceVariant else SecondaryTeal,
                        modifier = Modifier.clickable(enabled = !uiState.isSettingUpPaymentMethod) {
                            viewModel.startAddPaymentMethod()
                        }
                    )
                }

                val defaultCard = sub?.paymentMethods?.firstOrNull { it.isDefault }
                    ?: sub?.paymentMethods?.firstOrNull()
                val backupCards = sub?.paymentMethods
                    ?.filter { it.id != defaultCard?.id }
                    .orEmpty()

                if (sub?.paymentMethods.isNullOrEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.payments_no_methods),
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Primary Health Wallet Card
                    if (defaultCard != null) {
                        HealthWalletCard(card = defaultCard)
                    }

                    // Secondary Backup Cards
                    backupCards.forEach { backupCard ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.CreditCard,
                                        contentDescription = null,
                                        tint = PrimaryNavy,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = backupCard.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = OnSurface
                                        )
                                        Text(
                                            text = "${stringResource(R.string.payments_expires_label)} ${backupCard.expiry} • ${backupCard.note.orEmpty()}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = stringResource(R.string.payments_set_default),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = SecondaryTeal,
                                    modifier = Modifier.clickable {
                                        viewModel.setDefaultCard(backupCard.id)
                                    }
                                )
                            }
                        }
                    }
                }

                // Add New Card or Digital Wallet Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !uiState.isSettingUpPaymentMethod) {
                            viewModel.startAddPaymentMethod()
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFEBF1FD)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.isSettingUpPaymentMethod) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = PrimaryNavy
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.payments_opening_stripe),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryNavy
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.payments_add_new_card),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryNavy
                            )
                        }
                    }
                }
            }

            // Consultation Checkout Preferences Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = SurfaceContainerLowest,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.payments_checkout_prefs),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryNavy
                    )

                    // Auto Debit Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CreditCard,
                                contentDescription = null,
                                tint = SecondaryTeal,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.payments_auto_debit),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = OnSurface
                                )
                                Text(
                                    text = stringResource(R.string.payments_auto_debit_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = sub?.autoDebitEnabled ?: true,
                            onCheckedChange = { viewModel.toggleAutoDebit(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SecondaryTeal,
                                uncheckedThumbColor = OnSurfaceVariant,
                                uncheckedTrackColor = Color(0xFFE2E8F0)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Require FaceID Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Fingerprint,
                                contentDescription = null,
                                tint = SecondaryTeal,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.payments_face_id),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = OnSurface
                            )
                        }
                        Switch(
                            checked = sub?.requireFaceIdForCharges ?: true,
                            onCheckedChange = { viewModel.toggleFaceId(it) },
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

            // Recent Payment History Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.payments_history_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryNavy
                )

                PaymentHistoryFilterChips(
                    selectedCategory = uiState.selectedFilter,
                    onSelectCategory = { viewModel.setFilter(it) }
                )

                PaymentHistoryList(
                    items = uiState.filteredHistory,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }

    // Stripe WebView Dialog
    uiState.checkoutUrl?.let { url ->
        val isSetup = uiState.isCheckoutForSetup
        StripeCheckoutWebViewDialog(
            checkoutUrl = url,
            onSuccess = {
                val msg = if (isSetup) {
                    context.getString(R.string.payments_method_saved_toast)
                } else {
                    context.getString(R.string.payments_plan_updated_toast)
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                viewModel.closeCheckout(completed = true)
            },
            onCancelled = {
                val msg = if (isSetup) {
                    context.getString(R.string.payments_method_cancelled_toast)
                } else {
                    "Pago cancelado"
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                viewModel.closeCheckout(completed = false)
            },
            onDismissRequest = { viewModel.closeCheckout(completed = false) },
            onError = { msg -> viewModel.checkoutFailed(msg) }
        )
    }

    if (confirmDowngradeToFree) {
        AlertDialog(
            onDismissRequest = { confirmDowngradeToFree = false },
            title = { Text(stringResource(R.string.payments_plan_cancel_title)) },
            text = { Text(stringResource(R.string.payments_plan_cancel_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDowngradeToFree = false
                        viewModel.cancelSubscription()
                    }
                ) {
                    Text(stringResource(R.string.payments_plan_cancel_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDowngradeToFree = false }) {
                    Text(stringResource(R.string.shared_button_cancel))
                }
            }
        )
    }

    uiState.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text(stringResource(R.string.payments_setup_error_title)) },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissError) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        )
    }
}

@Composable
private fun SubscriptionPlanSection(
    currentPlan: SubscriptionPlan,
    activeUntil: String?,
    isBusy: Boolean,
    isUpdatingPlan: Boolean,
    onSelectPaidPlan: (SubscriptionPlan) -> Unit,
    onRequestDowngradeFree: () -> Unit
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
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.payments_plan_section),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryNavy
                )
                if (isUpdatingPlan) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = SecondaryTeal
                    )
                }
            }

            if (currentPlan != SubscriptionPlan.FREE && !activeUntil.isNullOrBlank()) {
                Text(
                    text = "Activo hasta: ${activeUntil.take(10)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }

            SubscriptionPlan.entries.forEach { plan ->
                val isCurrent = plan == currentPlan
                val desc = when (plan) {
                    SubscriptionPlan.FREE -> stringResource(R.string.payments_plan_free_desc)
                    SubscriptionPlan.TERRA -> stringResource(R.string.payments_plan_terra_desc)
                    SubscriptionPlan.ASTRUM -> stringResource(R.string.payments_plan_astrum_desc)
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isCurrent) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                    border = if (isCurrent) BorderStroke(1.dp, SecondaryTeal) else BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = plan.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryNavy
                                )
                                Text(
                                    text = plan.price,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = SecondaryTeal
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }

                        if (isCurrent) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF99F6E4)
                            ) {
                                Text(
                                    text = stringResource(R.string.payments_plan_current),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF0F766E),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else if (plan == SubscriptionPlan.FREE) {
                            OutlinedButton(
                                onClick = onRequestDowngradeFree,
                                enabled = !isBusy,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.payments_plan_downgrade_free),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        } else {
                            Button(
                                onClick = { onSelectPaidPlan(plan) },
                                enabled = !isBusy,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy, contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.payments_plan_choose),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
