package com.mindcluster.safediary.scheduling.infrastructure.remote.api

import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.AppointmentDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.BookableSlotDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.ClinicalSessionDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.ClinicianNameDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.CloseSessionDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.ContactRequestDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.CoordinationMessageDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.ProposeScheduleDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.RespondContactRequestDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.SendContactRequestDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.SendCoordinationMessageDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.SimulatedAppointmentPaymentDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Patient side of Care Scheduling. The identity comes from the bearer token. */
interface CareSchedulingApiService {

    @POST("api/v1/contact-requests")
    suspend fun sendContactRequest(@Body request: SendContactRequestDto): Response<ContactRequestDto>

    @GET("api/v1/contact-requests")
    suspend fun getContactRequests(): Response<List<ContactRequestDto>>

    @GET("api/v1/contact-requests/{id}/messages")
    suspend fun getMessages(@Path("id") contactRequestId: Long): Response<List<CoordinationMessageDto>>

    @POST("api/v1/contact-requests/{id}/messages")
    suspend fun sendMessage(
        @Path("id") contactRequestId: Long,
        @Body request: SendCoordinationMessageDto
    ): Response<CoordinationMessageDto>

    @GET("api/v1/appointments")
    suspend fun getAppointments(): Response<List<AppointmentDto>>

    @POST("api/v1/schedule/proposals/{appointmentId}/acceptance")
    suspend fun acceptProposal(@Path("appointmentId") appointmentId: Long): Response<AppointmentDto>

    @POST("api/v1/dev/care-scheduling/appointments/{appointmentId}/payment-result")
    suspend fun simulatePayment(
        @Path("appointmentId") appointmentId: Long,
        @Body request: SimulatedAppointmentPaymentDto
    ): Response<Map<String, String>>

    @POST("api/v1/appointments/{id}/cancellation")
    suspend fun cancelAppointment(@Path("id") appointmentId: Long): Response<AppointmentDto>

    @POST("api/v1/appointments/{appointmentId}/session/access")
    suspend fun joinSession(@Path("appointmentId") appointmentId: Long): Response<ClinicalSessionDto>

    /** Public Clinician Directory profile, read only to show the clinician name. */
    @GET("api/v1/clinicians/{id}")
    suspend fun getClinician(@Path("id") clinicianId: Long): Response<ClinicianNameDto>

    // Psychologist side (US-043 / US-044)

    /** Own Clinician Directory profile: its id identifies the psychologist's agenda. */
    @GET("api/v1/clinicians/me")
    suspend fun getOwnClinicianProfile(): Response<ClinicianNameDto>

    @PATCH("api/v1/contact-requests/{id}/decision")
    suspend fun respondContactRequest(
        @Path("id") contactRequestId: Long,
        @Body request: RespondContactRequestDto
    ): Response<ContactRequestDto>

    @GET("api/v1/schedule/clinicians/{clinicianId}/slots")
    suspend fun getSlots(
        @Path("clinicianId") clinicianId: Long,
        @Query("date") date: String
    ): Response<List<BookableSlotDto>>

    @POST("api/v1/schedule/proposals")
    suspend fun proposeSchedule(@Body request: ProposeScheduleDto): Response<AppointmentDto>

    @GET("api/v1/schedule/agenda")
    suspend fun getClinicianAgenda(): Response<List<AppointmentDto>>

    @GET("api/v1/appointments/{appointmentId}/session")
    suspend fun getSession(@Path("appointmentId") appointmentId: Long): Response<ClinicalSessionDto>

    @POST("api/v1/appointments/{appointmentId}/session/closure")
    suspend fun closeSession(
        @Path("appointmentId") appointmentId: Long,
        @Body request: CloseSessionDto
    ): Response<ClinicalSessionDto>
}
