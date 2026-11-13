package com.mindcluster.safediary.scheduling

import com.mindcluster.safediary.scheduling.domain.model.Appointment
import com.mindcluster.safediary.scheduling.domain.model.AppointmentStatus
import com.mindcluster.safediary.scheduling.infrastructure.remote.RetrofitCareSchedulingGateway
import com.mindcluster.safediary.scheduling.infrastructure.remote.SchedulingResult
import com.mindcluster.safediary.scheduling.infrastructure.remote.api.CareSchedulingApiService
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.*
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class ConsultationSkipPaymentTest {

    private class FakeCareSchedulingApiService(
        private val skipResponse: Response<SkipConsultationPaymentDto>,
        private val appointmentsResponse: Response<List<AppointmentDto>>
    ) : CareSchedulingApiService {
        var skipCalls = 0

        override suspend fun skipConsultationPayment(appointmentId: Long): Response<SkipConsultationPaymentDto> {
            skipCalls++
            return skipResponse
        }

        override suspend fun getAppointments(): Response<List<AppointmentDto>> = appointmentsResponse

        override suspend fun sendContactRequest(request: SendContactRequestDto): Response<ContactRequestDto> = error("unused")
        override suspend fun getContactRequests(): Response<List<ContactRequestDto>> = error("unused")
        override suspend fun getMessages(contactRequestId: Long): Response<List<CoordinationMessageDto>> = error("unused")
        override suspend fun sendMessage(contactRequestId: Long, request: SendCoordinationMessageDto): Response<CoordinationMessageDto> = error("unused")
        override suspend fun acceptProposal(appointmentId: Long): Response<AppointmentDto> = error("unused")
        override suspend fun simulatePayment(appointmentId: Long, request: SimulatedAppointmentPaymentDto): Response<Map<String, String>> = error("unused")
        override suspend fun startCheckout(request: ConsultationCheckoutRequestDto): Response<ConsultationCheckoutDto> = error("unused")
        override suspend fun cancelAppointment(appointmentId: Long): Response<AppointmentDto> = error("unused")
        override suspend fun joinSession(appointmentId: Long): Response<ClinicalSessionDto> = error("unused")
        override suspend fun getClinician(clinicianId: Long): Response<ClinicianNameDto> = error("unused")
        override suspend fun getOwnClinicianProfile(): Response<ClinicianNameDto> = error("unused")
        override suspend fun respondContactRequest(contactRequestId: Long, request: RespondContactRequestDto): Response<ContactRequestDto> = error("unused")
        override suspend fun getSlots(clinicianId: Long, date: String): Response<List<BookableSlotDto>> = error("unused")
        override suspend fun proposeSchedule(request: ProposeScheduleDto): Response<AppointmentDto> = error("unused")
        override suspend fun getClinicianAgenda(): Response<List<AppointmentDto>> = error("unused")
        override suspend fun getSession(appointmentId: Long): Response<ClinicalSessionDto> = error("unused")
        override suspend fun closeSession(appointmentId: Long, request: CloseSessionDto): Response<ClinicalSessionDto> = error("unused")
    }

    private fun heldAppointment(holdExpiresAt: Long) = Appointment(
        id = 42L,
        contactRequestId = 7L,
        clinicianId = 3L,
        startsAt = 1_800_000_000_000L,
        endsAt = 1_800_003_000_000L,
        amount = 120.0,
        currency = "PEN",
        status = AppointmentStatus.HELD,
        holdExpiresAt = holdExpiresAt,
        paymentReference = "care-appointment-42"
    )

    @Test
    fun skipPaymentConfirmsHeldAppointmentAndReloadsConfirmedStatus() = runTest {
        val api = FakeCareSchedulingApiService(
            skipResponse = Response.success(SkipConsultationPaymentDto(42L, "CONFIRMED", "SKIPPED_ONCE")),
            appointmentsResponse = Response.success(
                listOf(
                    AppointmentDto(
                        id = 42L,
                        contactRequestId = 7L,
                        clinicianId = 3L,
                        startsAt = "2027-01-15T15:00:00Z",
                        endsAt = "2027-01-15T15:50:00Z",
                        amount = 120.0,
                        currency = "PEN",
                        status = "CONFIRMED",
                        holdExpiresAt = null,
                        accessOpensAt = "2027-01-15T14:50:00Z",
                        accessClosesAt = "2027-01-15T16:00:00Z",
                        patientAccountId = 5L,
                        paymentReference = "care-appointment-42"
                    )
                )
            )
        )
        val gateway = RetrofitCareSchedulingGateway(api)

        val result = gateway.skipPayment(heldAppointment(System.currentTimeMillis() + 600_000L))

        assertEquals(1, api.skipCalls)
        assertTrue(result is SchedulingResult.Success)
        assertEquals(AppointmentStatus.CONFIRMED, (result as SchedulingResult.Success).value.status)
    }

    @Test
    fun skipPaymentRejectsExpiredHoldWithoutCallingApi() = runTest {
        val api = FakeCareSchedulingApiService(
            skipResponse = Response.success(SkipConsultationPaymentDto(42L, "CONFIRMED", "SKIPPED_ONCE")),
            appointmentsResponse = Response.success(emptyList())
        )
        val gateway = RetrofitCareSchedulingGateway(api)

        val result = gateway.skipPayment(heldAppointment(System.currentTimeMillis() - 10_000L))

        assertEquals(0, api.skipCalls)
        assertTrue(result is SchedulingResult.Failure)
        assertEquals(409, (result as SchedulingResult.Failure).status)
    }

    @Test
    fun skipPaymentPropagatesBackendFailure() = runTest {
        val api = FakeCareSchedulingApiService(
            skipResponse = Response.error(422, "{}".toResponseBody("application/json".toMediaType())),
            appointmentsResponse = Response.success(emptyList())
        )
        val gateway = RetrofitCareSchedulingGateway(api)

        val result = gateway.skipPayment(heldAppointment(System.currentTimeMillis() + 600_000L))

        assertEquals(1, api.skipCalls)
        assertTrue(result is SchedulingResult.Failure)
        assertEquals(422, (result as SchedulingResult.Failure).status)
    }
}
