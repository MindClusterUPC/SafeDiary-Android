package com.mindcluster.safediary.payments.infrastructure.remote.api

import com.mindcluster.safediary.payments.infrastructure.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface PaymentsApiService {
    @POST
    suspend fun createSubscriptionCheckout(
        @Url url: String,
        @Body request: CreateSubscriptionCheckoutRequestDto,
        @Header("Idempotency-Key") idempotencyKey: String? = null
    ): Response<SubscriptionCheckoutResponseDto>

    @GET
    suspend fun getCurrentSubscription(
        @Url url: String,
        @Query("patientAccountId") patientAccountId: Long
    ): Response<SubscriptionResponseDto>

    @POST
    suspend fun cancelSubscription(
        @Url url: String,
        @Body request: CancelSubscriptionRequestDto
    ): Response<SubscriptionResponseDto>

    @GET
    suspend fun getPaymentHistory(
        @Url url: String,
        @Query("patientAccountId") patientAccountId: Long
    ): Response<List<PaymentHistoryItemDto>>

    @GET
    suspend fun getPaymentMethods(
        @Url url: String,
        @Query("patientAccountId") patientAccountId: Long
    ): Response<List<PaymentMethodItemDto>>

    @POST
    suspend fun createPaymentMethodSetup(
        @Url url: String,
        @Body request: CreatePaymentMethodSetupRequestDto
    ): Response<PaymentMethodSetupResponseDto>

    @PUT
    suspend fun setDefaultPaymentMethod(
        @Url url: String,
        @Query("patientAccountId") patientAccountId: Long
    ): Response<List<PaymentMethodItemDto>>
}
