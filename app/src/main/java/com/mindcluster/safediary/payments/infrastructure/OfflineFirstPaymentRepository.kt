package com.mindcluster.safediary.payments.infrastructure

import com.mindcluster.safediary.payments.domain.model.*
import com.mindcluster.safediary.payments.domain.repository.PatientPaymentRepository
import com.mindcluster.safediary.payments.infrastructure.local.PaymentPreferences
import com.mindcluster.safediary.payments.infrastructure.remote.RetrofitPaymentGateway
import com.mindcluster.safediary.payments.infrastructure.remote.dto.PaymentMethodItemDto
import com.mindcluster.safediary.shared.domain.events.DomainEventPublisher
import com.mindcluster.safediary.shared.infrastructure.network.ApiEndpoints
import com.mindcluster.safediary.shared.infrastructure.network.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OfflineFirstPaymentRepository(
    private val preferences: PaymentPreferences,
    private val remoteGateway: RetrofitPaymentGateway,
    private val networkMonitor: NetworkMonitor,
    private val eventPublisher: DomainEventPublisher
) : PatientPaymentRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val subscriptionState = MutableStateFlow<PatientSubscriptionAggregate?>(null)

    init {
        subscriptionState.value = buildFromPreferences(1L)
    }

    override fun observeSubscription(patientAccountId: Long): Flow<PatientSubscriptionAggregate?> =
        subscriptionState.asStateFlow()

    override suspend fun getSubscription(patientAccountId: Long): PatientSubscriptionAggregate {
        val current = subscriptionState.value ?: buildFromPreferences(patientAccountId)

        if (networkMonitor.isOnline.value) {
            try {
                val remoteDto = remoteGateway.getCurrentSubscription(patientAccountId)
                if (remoteDto != null) {
                    val plan = SubscriptionPlan.fromString(remoteDto.plan)
                    val status = SubscriptionStatus.fromString(remoteDto.status)
                    current.updatePlan(plan, status, remoteDto.activeUntil)
                    preferences.currentPlan = plan.name
                    preferences.subscriptionStatus = status.name
                }
                val methodsDto = remoteGateway.getPaymentMethods(patientAccountId)
                if (methodsDto != null) {
                    current.paymentMethods = methodsDto.map { m ->
                        mapPaymentMethod(m)
                    }
                }
                subscriptionState.value = current
            } catch (e: Exception) {
                // If offline or network error, retain local state
            }
        }
        return current
    }

    override suspend fun createPaymentMethodSetup(patientAccountId: Long): String {
        if (!networkMonitor.isOnline.value) {
            throw IllegalStateException("Sin conexión a internet. Conéctate para agregar un método de pago.")
        }
        val dto = remoteGateway.createPaymentMethodSetup(patientAccountId).getOrThrow()
        val url = dto.checkoutUrl?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException(dto.message ?: "El servidor no devolvió el enlace de Stripe.")
        return url
    }

    override suspend fun createSubscriptionCheckout(
        patientAccountId: Long,
        plan: SubscriptionPlan
    ): String? {
        if (plan == SubscriptionPlan.FREE) {
            if (networkMonitor.isOnline.value) {
                remoteGateway.createSubscriptionCheckout(patientAccountId, plan.name)
            }
            val current = subscriptionState.value ?: buildFromPreferences(patientAccountId)
            current.updatePlan(SubscriptionPlan.FREE, SubscriptionStatus.ACTIVE, null)
            preferences.currentPlan = SubscriptionPlan.FREE.name
            preferences.subscriptionStatus = SubscriptionStatus.ACTIVE.name
            subscriptionState.value = current
            dispatchEvents(current)
            return null
        }

        if (!networkMonitor.isOnline.value) {
            throw IllegalStateException("Sin conexión a internet. Conéctate para cambiar de plan.")
        }

        val response = remoteGateway.createSubscriptionCheckout(patientAccountId, plan.name)
            ?: throw IllegalStateException("No se pudo iniciar el cambio de plan con Stripe. Inténtalo de nuevo.")

        return response.checkoutUrl?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException(response.message ?: "El servidor no devolvió el enlace de Stripe.")
    }

    override suspend fun cancelSubscription(patientAccountId: Long): PatientSubscriptionAggregate {
        val current = subscriptionState.value ?: buildFromPreferences(patientAccountId)
        current.cancelSubscription()
        preferences.currentPlan = SubscriptionPlan.FREE.name
        preferences.subscriptionStatus = SubscriptionStatus.CANCELLED.name
        subscriptionState.value = current

        if (networkMonitor.isOnline.value) {
            remoteGateway.cancelSubscription(patientAccountId)
        }

        dispatchEvents(current)
        return current
    }

    override suspend fun updatePreferences(
        patientAccountId: Long,
        autoDebit: Boolean,
        requireFaceId: Boolean
    ): PatientSubscriptionAggregate {
        val current = subscriptionState.value ?: buildFromPreferences(patientAccountId)
        current.updatePreferences(autoDebit, requireFaceId)
        preferences.autoDebitEnabled = autoDebit
        preferences.requireFaceIdForCharges = requireFaceId
        subscriptionState.value = current
        return current
    }

    override suspend fun setDefaultPaymentMethod(
        patientAccountId: Long,
        cardId: String
    ): PatientSubscriptionAggregate {
        val current = subscriptionState.value ?: buildFromPreferences(patientAccountId)
        current.setDefaultCard(cardId)
        preferences.defaultCardId = cardId

        if (networkMonitor.isOnline.value) {
            val remoteMethods = remoteGateway.setDefaultPaymentMethod(patientAccountId, cardId)
            if (remoteMethods != null) {
                current.paymentMethods = remoteMethods.map(::mapPaymentMethod)
            }
        }

        subscriptionState.value = current
        return current
    }

    private fun mapPaymentMethod(m: PaymentMethodItemDto): PaymentMethodCard = PaymentMethodCard(
        id = m.id,
        title = if (m.brand.equals("VISA", ignoreCase = true)) {
            "Visa terminada en ${m.last4}"
        } else {
            "${m.brand} terminada en ${m.last4}"
        },
        brand = m.brand,
        last4 = m.last4,
        cardholder = m.cardholder,
        expiry = m.expiry,
        isDefault = m.isDefault,
        note = m.note
    )

    override suspend fun getPaymentHistory(patientAccountId: Long): List<PaymentHistoryItem> {
        if (networkMonitor.isOnline.value) {
            val remoteHistory = remoteGateway.getPaymentHistory(patientAccountId)
            if (remoteHistory != null) {
                return remoteHistory.map { h ->
                    PaymentHistoryItem(
                        id = h.id,
                        title = h.title,
                        description = h.description,
                        amountFormatted = h.amountFormatted,
                        referenceNumber = h.referenceNumber,
                        category = if (h.category.equals("SUBSCRIPTION", ignoreCase = true)) {
                            PaymentCategory.SUBSCRIPTION
                        } else {
                            PaymentCategory.CONSULTATION
                        },
                        initials = h.initials
                    )
                }
            }
        }

        return emptyList()
    }

    private fun buildFromPreferences(patientAccountId: Long): PatientSubscriptionAggregate {
        val plan = SubscriptionPlan.fromString(preferences.currentPlan)
        val status = SubscriptionStatus.fromString(preferences.subscriptionStatus)
        val aggregate = PatientSubscriptionAggregate(
            patientAccountId = patientAccountId,
            plan = plan,
            status = status,
            autoDebitEnabled = preferences.autoDebitEnabled,
            requireFaceIdForCharges = preferences.requireFaceIdForCharges
        )
        aggregate.setDefaultCard(preferences.defaultCardId)
        return aggregate
    }

    private fun dispatchEvents(aggregate: PatientSubscriptionAggregate) {
        aggregate.getDomainEvents().forEach { event ->
            scope.launch { eventPublisher.publish(event) }
        }
        aggregate.clearEvents()
    }
}
