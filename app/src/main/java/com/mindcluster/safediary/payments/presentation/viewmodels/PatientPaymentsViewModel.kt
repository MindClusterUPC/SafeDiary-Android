package com.mindcluster.safediary.payments.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mindcluster.safediary.payments.application.commands.*
import com.mindcluster.safediary.payments.application.queries.*
import com.mindcluster.safediary.payments.domain.model.PatientSubscriptionAggregate
import com.mindcluster.safediary.payments.domain.model.PaymentCategory
import com.mindcluster.safediary.payments.domain.model.PaymentHistoryItem
import com.mindcluster.safediary.payments.domain.model.SubscriptionPlan
import com.mindcluster.safediary.payments.infrastructure.PaymentsModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PatientPaymentsUiState(
    val subscription: PatientSubscriptionAggregate? = null,
    val history: List<PaymentHistoryItem> = emptyList(),
    val selectedFilter: PaymentCategory? = null,
    val checkoutUrl: String? = null,
    val isCheckoutForSetup: Boolean = false,
    val isSettingUpPaymentMethod: Boolean = false,
    val isUpdatingPlan: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val filteredHistory: List<PaymentHistoryItem>
        get() = if (selectedFilter == null) history else history.filter { it.category == selectedFilter }
}

class PatientPaymentsViewModel(
    application: Application,
    private val getCurrentSubscriptionHandler: GetCurrentSubscriptionHandler,
    private val createSubscriptionCheckoutHandler: CreateSubscriptionCheckoutHandler,
    private val createPaymentMethodSetupHandler: CreatePaymentMethodSetupHandler,
    private val cancelSubscriptionHandler: CancelSubscriptionHandler,
    private val updatePaymentPreferencesHandler: UpdatePaymentPreferencesHandler,
    private val getPaymentHistoryHandler: GetPaymentHistoryHandler,
    private val setDefaultPaymentMethodHandler: SetDefaultPaymentMethodHandler,
    private val patientAccountId: Long = 1L
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PatientPaymentsUiState(isLoading = true))
    val uiState: StateFlow<PatientPaymentsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val subResult = getCurrentSubscriptionHandler.handle(GetCurrentSubscriptionQuery(patientAccountId))
            val histResult = getPaymentHistoryHandler.handle(GetPaymentHistoryQuery(patientAccountId))
            _uiState.update {
                it.copy(
                    subscription = subResult.getOrNull(),
                    history = histResult.getOrDefault(emptyList()),
                    isLoading = false
                )
            }
        }
    }

    fun setFilter(category: PaymentCategory?) {
        _uiState.update { it.copy(selectedFilter = category) }
    }

    fun toggleAutoDebit(enabled: Boolean) {
        val currentSub = _uiState.value.subscription ?: return
        viewModelScope.launch {
            val result = updatePaymentPreferencesHandler.handle(
                UpdatePaymentPreferencesCommand(patientAccountId, enabled, currentSub.requireFaceIdForCharges)
            )
            result.getOrNull()?.let { updated ->
                _uiState.update { it.copy(subscription = updated) }
            }
        }
    }

    fun toggleFaceId(enabled: Boolean) {
        val currentSub = _uiState.value.subscription ?: return
        viewModelScope.launch {
            val result = updatePaymentPreferencesHandler.handle(
                UpdatePaymentPreferencesCommand(patientAccountId, currentSub.autoDebitEnabled, enabled)
            )
            result.getOrNull()?.let { updated ->
                _uiState.update { it.copy(subscription = updated) }
            }
        }
    }

    fun setDefaultCard(cardId: String) {
        viewModelScope.launch {
            val result = setDefaultPaymentMethodHandler.handle(
                SetDefaultPaymentMethodCommand(patientAccountId, cardId)
            )
            result.getOrNull()?.let { updated ->
                _uiState.update { it.copy(subscription = updated) }
            }
        }
    }

    fun startCheckout(plan: SubscriptionPlan) {
        if (_uiState.value.isUpdatingPlan || _uiState.value.isSettingUpPaymentMethod) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingPlan = true, errorMessage = null) }
            val result = createSubscriptionCheckoutHandler.handle(
                CreateSubscriptionCheckoutCommand(patientAccountId, plan)
            )
            result.fold(
                onSuccess = { checkoutUrl ->
                    _uiState.update { it.copy(isUpdatingPlan = false) }
                    if (!checkoutUrl.isNullOrBlank()) {
                        _uiState.update { it.copy(checkoutUrl = checkoutUrl, isCheckoutForSetup = false) }
                    } else {
                        loadData()
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isUpdatingPlan = false,
                            errorMessage = error.message ?: "No se pudo cambiar el plan de suscripción."
                        )
                    }
                }
            )
        }
    }

    fun cancelSubscription() {
        if (_uiState.value.isUpdatingPlan || _uiState.value.isSettingUpPaymentMethod) return
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingPlan = true, errorMessage = null) }
            val result = cancelSubscriptionHandler.handle(CancelSubscriptionCommand(patientAccountId))
            result.fold(
                onSuccess = { updated ->
                    _uiState.update { it.copy(subscription = updated, isUpdatingPlan = false) }
                    loadData()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isUpdatingPlan = false,
                            errorMessage = error.message ?: "No se pudo cancelar la suscripción."
                        )
                    }
                }
            )
        }
    }

    fun startAddPaymentMethod() {
        if (_uiState.value.isSettingUpPaymentMethod || _uiState.value.isUpdatingPlan) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSettingUpPaymentMethod = true, errorMessage = null) }
            val result = createPaymentMethodSetupHandler.handle(
                CreatePaymentMethodSetupCommand(patientAccountId)
            )
            result.fold(
                onSuccess = { setupUrl ->
                    _uiState.update {
                        it.copy(
                            checkoutUrl = setupUrl,
                            isCheckoutForSetup = true,
                            isSettingUpPaymentMethod = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSettingUpPaymentMethod = false,
                            errorMessage = error.message ?: "No se pudo abrir Stripe para guardar el método de pago."
                        )
                    }
                }
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun checkoutFailed(message: String) {
        _uiState.update { it.copy(checkoutUrl = null, isCheckoutForSetup = false, errorMessage = message) }
    }

    fun closeCheckout(completed: Boolean = false) {
        val wasSetup = _uiState.value.isCheckoutForSetup
        _uiState.update { it.copy(checkoutUrl = null, isCheckoutForSetup = false) }
        if (completed && !wasSetup) {
            viewModelScope.launch {
                loadData()
                delay(1500)
                loadData()
            }
        } else {
            loadData()
        }
    }

    class Factory(
        private val application: Application,
        private val paymentsModule: PaymentsModule
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PatientPaymentsViewModel(
                application = application,
                getCurrentSubscriptionHandler = paymentsModule.getCurrentSubscriptionHandler,
                createSubscriptionCheckoutHandler = paymentsModule.createSubscriptionCheckoutHandler,
                createPaymentMethodSetupHandler = paymentsModule.createPaymentMethodSetupHandler,
                cancelSubscriptionHandler = paymentsModule.cancelSubscriptionHandler,
                updatePaymentPreferencesHandler = paymentsModule.updatePaymentPreferencesHandler,
                getPaymentHistoryHandler = paymentsModule.getPaymentHistoryHandler,
                setDefaultPaymentMethodHandler = paymentsModule.setDefaultPaymentMethodHandler,
                patientAccountId = paymentsModule.authSessionPreferences.patientAccountId
            ) as T
        }
    }
}
