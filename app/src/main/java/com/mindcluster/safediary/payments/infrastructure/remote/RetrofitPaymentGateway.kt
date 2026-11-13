package com.mindcluster.safediary.payments.infrastructure.remote

import android.util.Log
import com.mindcluster.safediary.payments.infrastructure.remote.api.PaymentsApiService
import com.mindcluster.safediary.payments.infrastructure.remote.dto.*
import com.mindcluster.safediary.shared.infrastructure.network.ApiEndpoints
import com.mindcluster.safediary.shared.infrastructure.network.RetrofitClientProvider

class RetrofitPaymentGateway(
    private val apiService: PaymentsApiService = RetrofitClientProvider.createService()
) {
    private val tag = "PaymentGateway"

    suspend fun createSubscriptionCheckout(
        patientAccountId: Long,
        plan: String
    ): SubscriptionCheckoutResponseDto? = try {
        val response = apiService.createSubscriptionCheckout(
            url = ApiEndpoints.paymentsCheckout,
            request = CreateSubscriptionCheckoutRequestDto(
                patientAccountId = patientAccountId,
                plan = plan
            )
        )
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "createSubscriptionCheckout error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: Exception) {
        Log.w(tag, "createSubscriptionCheckout network error: ${e.message}")
        null
    }

    suspend fun getCurrentSubscription(
        patientAccountId: Long
    ): SubscriptionResponseDto? = try {
        val response = apiService.getCurrentSubscription(
            url = ApiEndpoints.paymentsCurrent,
            patientAccountId = patientAccountId
        )
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "getCurrentSubscription error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: Exception) {
        Log.w(tag, "getCurrentSubscription network error: ${e.message}")
        null
    }

    suspend fun cancelSubscription(
        patientAccountId: Long
    ): SubscriptionResponseDto? = try {
        val response = apiService.cancelSubscription(
            url = ApiEndpoints.paymentsCancel,
            request = CancelSubscriptionRequestDto(patientAccountId)
        )
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "cancelSubscription error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: Exception) {
        Log.w(tag, "cancelSubscription network error: ${e.message}")
        null
    }

    suspend fun getPaymentHistory(
        patientAccountId: Long
    ): List<PaymentHistoryItemDto>? = try {
        val response = apiService.getPaymentHistory(
            url = ApiEndpoints.paymentsHistory,
            patientAccountId = patientAccountId
        )
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "getPaymentHistory error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: Exception) {
        Log.w(tag, "getPaymentHistory network error: ${e.message}")
        null
    }

    suspend fun getPaymentMethods(
        patientAccountId: Long
    ): List<PaymentMethodItemDto>? = try {
        val response = apiService.getPaymentMethods(
            url = ApiEndpoints.paymentsMethods,
            patientAccountId = patientAccountId
        )
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "getPaymentMethods error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: Exception) {
        Log.w(tag, "getPaymentMethods network error: ${e.message}")
        null
    }

    suspend fun createPaymentMethodSetup(
        patientAccountId: Long
    ): Result<PaymentMethodSetupResponseDto> = try {
        val response = apiService.createPaymentMethodSetup(
            url = ApiEndpoints.paymentsMethodsSetup,
            request = CreatePaymentMethodSetupRequestDto(patientAccountId = patientAccountId)
        )
        val body = response.body()
        if (response.isSuccessful && body != null) {
            Result.success(body)
        } else {
            val detail = response.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: response.message()
            Log.w(tag, "createPaymentMethodSetup error ${response.code()}: $detail")
            Result.failure(IllegalStateException("No se pudo iniciar el registro de pago (${response.code()})"))
        }
    } catch (e: Exception) {
        Log.w(tag, "createPaymentMethodSetup network error: ${e.message}")
        Result.failure(e)
    }

    suspend fun setDefaultPaymentMethod(
        patientAccountId: Long,
        paymentMethodId: String
    ): List<PaymentMethodItemDto>? = try {
        val response = apiService.setDefaultPaymentMethod(
            url = ApiEndpoints.paymentMethodDefault(paymentMethodId),
            patientAccountId = patientAccountId
        )
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "setDefaultPaymentMethod error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: Exception) {
        Log.w(tag, "setDefaultPaymentMethod network error: ${e.message}")
        null
    }
}
