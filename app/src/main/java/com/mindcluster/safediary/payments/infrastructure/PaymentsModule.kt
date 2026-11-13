package com.mindcluster.safediary.payments.infrastructure

import android.content.Context
import com.mindcluster.safediary.payments.application.commands.*
import com.mindcluster.safediary.payments.application.queries.*
import com.mindcluster.safediary.payments.domain.repository.PatientPaymentRepository
import com.mindcluster.safediary.payments.infrastructure.local.PaymentPreferences
import com.mindcluster.safediary.payments.infrastructure.remote.RetrofitPaymentGateway
import com.mindcluster.safediary.shared.domain.events.DomainEventPublisher
import com.mindcluster.safediary.shared.infrastructure.eventbus.InMemoryDomainEventBus
import com.mindcluster.safediary.shared.infrastructure.network.NetworkMonitor
import com.mindcluster.safediary.shared.infrastructure.security.AuthSessionPreferences

class PaymentsModule(context: Context) {
    val authSessionPreferences: AuthSessionPreferences by lazy { AuthSessionPreferences(context) }
    val paymentPreferences: PaymentPreferences by lazy { PaymentPreferences(context) }
    val networkMonitor: NetworkMonitor by lazy { NetworkMonitor.getInstance(context) }
    val eventBus: DomainEventPublisher by lazy { InMemoryDomainEventBus() }
    val remoteGateway: RetrofitPaymentGateway by lazy { RetrofitPaymentGateway() }

    val repository: PatientPaymentRepository by lazy {
        OfflineFirstPaymentRepository(
            paymentPreferences,
            remoteGateway,
            networkMonitor,
            eventBus
        )
    }

    val getCurrentSubscriptionHandler: GetCurrentSubscriptionHandler by lazy {
        GetCurrentSubscriptionHandler(repository)
    }

    val createSubscriptionCheckoutHandler: CreateSubscriptionCheckoutHandler by lazy {
        CreateSubscriptionCheckoutHandler(repository)
    }

    val createPaymentMethodSetupHandler: CreatePaymentMethodSetupHandler by lazy {
        CreatePaymentMethodSetupHandler(repository)
    }

    val cancelSubscriptionHandler: CancelSubscriptionHandler by lazy {
        CancelSubscriptionHandler(repository)
    }

    val updatePaymentPreferencesHandler: UpdatePaymentPreferencesHandler by lazy {
        UpdatePaymentPreferencesHandler(repository)
    }

    val getPaymentHistoryHandler: GetPaymentHistoryHandler by lazy {
        GetPaymentHistoryHandler(repository)
    }

    val setDefaultPaymentMethodHandler: SetDefaultPaymentMethodHandler by lazy {
        SetDefaultPaymentMethodHandler(repository)
    }

    companion object {
        @Volatile
        private var instance: PaymentsModule? = null

        fun getInstance(context: Context): PaymentsModule {
            return instance ?: synchronized(this) {
                instance ?: PaymentsModule(context.applicationContext).also { instance = it }
            }
        }
    }
}
