package com.mindcluster.safediary.scheduling

import com.google.gson.Gson
import com.mindcluster.safediary.scheduling.domain.model.AppointmentStatus
import com.mindcluster.safediary.scheduling.infrastructure.remote.RetrofitCareSchedulingGateway
import com.mindcluster.safediary.scheduling.infrastructure.remote.SchedulingResult
import com.mindcluster.safediary.scheduling.infrastructure.remote.localPaymentSimulationAvailable
import com.mindcluster.safediary.scheduling.infrastructure.remote.api.CareSchedulingApiService
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.AppointmentDto
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.SimulatedAppointmentPaymentDto
import java.lang.reflect.Proxy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class LocalAppointmentPaymentTest {
    private fun appointment(status: String) = Gson().fromJson(
        """{"id":5,"contactRequestId":5,"clinicianId":3,"patientAccountId":10,
          "startsAt":"2030-11-12T20:00:00Z","endsAt":"2030-11-12T20:50:00Z",
          "status":"$status","amount":100.0,"currency":"PEN",
          "holdExpiresAt":"2030-11-12T20:00:00Z",
          "paymentReference":"reference-issued-by-backend"}""",
        AppointmentDto::class.java
    )

    private fun service(
        appointments: () -> List<AppointmentDto>,
        onPayment: (Long, SimulatedAppointmentPaymentDto) -> Unit
    ) = Proxy.newProxyInstance(
        CareSchedulingApiService::class.java.classLoader,
        arrayOf(CareSchedulingApiService::class.java)
    ) { _, method, args ->
        when (method.name) {
            "getAppointments" -> Response.success(appointments())
            "simulatePayment" -> {
                onPayment(args!![0] as Long, args[1] as SimulatedAppointmentPaymentDto)
                Response.success(mapOf("message" to "CONFIRMED"))
            }
            else -> throw UnsupportedOperationException(method.name)
        }
    } as CareSchedulingApiService

    @Test fun simulationIsLimitedToDebugBuildsAndLocalHttpBackends() {
        listOf("http://127.0.0.1:8080/", "http://10.0.2.2:8080/", "http://localhost:8080/").forEach {
            assertTrue(localPaymentSimulationAvailable(true, it))
            assertFalse(localPaymentSimulationAvailable(false, it))
        }
        listOf("https://safediary-platform.onrender.com/", "http://example.com/",
            "http://localhost.example.com/", "http://127.0.0.1@example.com/", "invalid").forEach {
            assertFalse(localPaymentSimulationAvailable(true, it))
        }
    }

    @Test fun usesTheReservationReferenceAndReadsTheConfirmedStateFromTheBackend() = runTest {
        var paid = false
        var calls = 0
        val gateway = RetrofitCareSchedulingGateway(service(
            appointments = { listOf(appointment(if (paid) "CONFIRMED" else "HELD")) },
            onPayment = { id, payment ->
                calls++
                assertEquals(5L, id)
                assertEquals("reference-issued-by-backend", payment.paymentReference)
                assertTrue(payment.approved)
                assertEquals(100.0, payment.amount, 0.001)
                assertEquals("PEN", payment.currency)
                paid = true
            }
        ))
        val held = (gateway.getAppointments() as SchedulingResult.Success).value.single()
        val result = gateway.simulatePayment(held) as SchedulingResult.Success
        assertEquals(AppointmentStatus.CONFIRMED, result.value.status)
        assertEquals(1, calls)
    }

    @Test fun successfulPaymentResponseAloneCannotConfirmTheAppointment() = runTest {
        val gateway = RetrofitCareSchedulingGateway(service({ listOf(appointment("HELD")) }) { _, _ -> })
        val held = (gateway.getAppointments() as SchedulingResult.Success).value.single()
        val result = gateway.simulatePayment(held) as SchedulingResult.Failure
        assertEquals(409, result.status)
    }

    @Test fun expiredOrMissingPaymentReferencesNeverSendAPaymentRequest() = runTest {
        var calls = 0
        val gateway = RetrofitCareSchedulingGateway(service({ listOf(appointment("HELD")) }) { _, _ -> calls++ })
        val held = (gateway.getAppointments() as SchedulingResult.Success).value.single()
        assertEquals(409, (gateway.simulatePayment(held.copy(holdExpiresAt = 0)) as SchedulingResult.Failure).status)
        assertEquals(422, (gateway.simulatePayment(held.copy(paymentReference = null)) as SchedulingResult.Failure).status)
        assertEquals(0, calls)
    }
}
