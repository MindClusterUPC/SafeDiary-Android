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
                        color = SecondaryTeal,
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Agregar método de pago", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                val defaultCard = sub?.paymentMethods?.firstOrNull { it.isDefault }
                val backupCard = sub?.paymentMethods?.firstOrNull { !it.isDefault }

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

                    // Secondary Backup Card
                    if (backupCard != null) {
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
                        .clickable {
                            Toast.makeText(context, "Configurar nueva tarjeta...", Toast.LENGTH_SHORT).show()
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
                        Text(
                            text = stringResource(R.string.payments_add_new_card),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryNavy
                        )
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
        StripeCheckoutWebViewDialog(
            checkoutUrl = url,
            onSuccess = {
                Toast.makeText(context, "Pago completado", Toast.LENGTH_SHORT).show()
                viewModel.closeCheckout()
            },
            onCancelled = {
                Toast.makeText(context, "Pago cancelado", Toast.LENGTH_SHORT).show()
                viewModel.closeCheckout()
            },
            onDismissRequest = { viewModel.closeCheckout() }
        )
    }
}
