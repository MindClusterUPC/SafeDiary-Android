package com.mindcluster.safediary.patient

import com.mindcluster.safediary.professionals.domain.model.Professional
import com.mindcluster.safediary.professionals.infrastructure.remote.dto.ClinicianProfileDto
import com.mindcluster.safediary.professionals.infrastructure.remote.dto.ConsultationRateDto
import com.mindcluster.safediary.professionals.infrastructure.remote.initialsOf
import com.mindcluster.safediary.professionals.infrastructure.remote.toProfessional
import com.mindcluster.safediary.professionals.presentation.viewmodels.ProfessionalsUiState
import com.mindcluster.safediary.rutines.presentation.viewmodels.RutinesViewModel
import com.mindcluster.safediary.scheduling.domain.model.Appointment
import com.mindcluster.safediary.scheduling.domain.model.AppointmentStatus
import com.mindcluster.safediary.scheduling.domain.model.BookableSlot
import com.mindcluster.safediary.scheduling.domain.model.ContactRequest
import com.mindcluster.safediary.scheduling.domain.model.ContactRequestStatus
import com.mindcluster.safediary.scheduling.domain.model.SlotAvailability
import com.mindcluster.safediary.scheduling.domain.model.patientLabel
import com.mindcluster.safediary.scheduling.infrastructure.remote.dto.parseInstantMillis
import com.mindcluster.safediary.scheduling.presentation.viewmodels.ClinicianAgendaUiState
import com.mindcluster.safediary.scheduling.presentation.viewmodels.SchedulingUiState
import com.mindcluster.safediary.scheduling.presentation.viewmodels.agendaDate
import com.mindcluster.safediary.scheduling.presentation.viewmodels.expiredLocally
import com.mindcluster.safediary.scheduling.presentation.viewmodels.startOfDay
import com.mindcluster.safediary.scheduling.presentation.viewmodels.weekDays
import org.junit.Assert.*
import org.junit.Test

class PatientPreviewTest {
    private val now = 1_800_000_000_000L
    private fun appointment(id: Long, status: AppointmentStatus, contactRequestId: Long = 1L) = Appointment(
        id = id, contactRequestId = contactRequestId, clinicianId = 7L, startsAt = now + 3_600_000, endsAt = now + 6_600_000,
        amount = 120.0, currency = "PEN", status = status, holdExpiresAt = now + 3_600_000,
        accessOpensAt = now - 60_000, accessClosesAt = now + 60_000
    )

    @Test fun expiredHoldIsShownAsExpiredAndIsNoLongerActive() {
        val held = appointment(1, AppointmentStatus.HELD)
        assertTrue(held.isHoldActive(now))
        assertEquals(AppointmentStatus.HELD, held.expiredLocally(now).status)
        val expiry = held.holdExpiresAt!!
        assertFalse(held.isHoldActive(expiry))
        assertEquals(AppointmentStatus.EXPIRED, held.expiredLocally(expiry).status)
        assertEquals(0L, held.remainingSeconds(expiry + 5_000))
    }

    @Test fun callAccessRequiresConfirmedAppointmentAndItsAccessWindow() {
        val confirmed = appointment(1, AppointmentStatus.CONFIRMED)
        assertTrue(confirmed.canJoin(now))
        assertFalse(confirmed.canJoin(confirmed.accessOpensAt!! - 1))
        assertFalse(confirmed.canJoin(confirmed.accessClosesAt!! + 1))
        assertFalse(appointment(2, AppointmentStatus.HELD).canJoin(now))
        assertFalse(confirmed.copy(accessOpensAt = null, accessClosesAt = null).canJoin(now))
    }

    @Test fun chatShowsOnlyItsLatestOpenProposalAndItsHold() {
        val state = SchedulingUiState(appointments = listOf(
            appointment(1, AppointmentStatus.CANCELLED),
            appointment(2, AppointmentStatus.REQUESTED),
            appointment(3, AppointmentStatus.REQUESTED),
            appointment(4, AppointmentStatus.REQUESTED, contactRequestId = 9L),
            appointment(5, AppointmentStatus.HELD, contactRequestId = 9L)
        ))
        assertEquals(3L, state.proposalFor(1L)?.id)
        assertNull(state.heldFor(1L))
        assertEquals(5L, state.heldFor(9L)?.id)
        assertNull(state.proposalFor(2L))
    }

    @Test fun backendInstantsAreParsedInUtc() {
        assertEquals(1_762_614_000_000L, parseInstantMillis("2025-11-08T15:00:00Z"))
        assertEquals(1_762_614_000_601L, parseInstantMillis("2025-11-08T15:00:00.601123Z"))
        assertNull(parseInstantMillis(null))
        assertNull(parseInstantMillis("not a date"))
    }

    @Test fun searchIgnoresAccentsAndFiltersCompose() {
        fun professional(id: String, name: String, price: Int, tags: List<String>) =
            Professional(id, name, initialsOf(name), "Psicología clínica", tags, "", price)
        var state = ProfessionalsUiState(professionals = listOf(
            professional("1", "Dra. Laura Gómez", 120, listOf("Ansiedad")),
            professional("2", "Ps. Liam Thorne", 65, listOf("Estrés"))
        ))
        assertEquals("1", state.copy(query = "gomez").results.single().id)
        assertTrue(state.copy(query = "gomez", affordableOnly = true).results.isEmpty())
        assertEquals(listOf("Todos", "Ansiedad", "Estrés"), state.categories)
        state = state.copy(favorites = setOf("2"), favoritesOnly = true)
        assertEquals("2", state.results.single().id)
    }

    @Test fun agendaWeekRunsFromMondayToSundayInLimaTime() {
        // Thursday 2026-10-08 20:00 in Lima is already Friday in UTC.
        val thursdayNight = parseInstantMillis("2026-10-09T01:00:00Z")!!
        val week = weekDays(thursdayNight).map { agendaDate(it) }
        assertEquals(listOf("2026-10-05", "2026-10-06", "2026-10-07", "2026-10-08", "2026-10-09", "2026-10-10", "2026-10-11"), week)
        assertEquals("2026-10-08", agendaDate(startOfDay(thursdayNight)))
    }

    @Test fun psychologistCanOnlyProposeToAnOpenRequest() {
        val open = ContactRequest(1L, 7L, ContactRequestStatus.PENDING, patientAccountId = 20L)
        val rejected = ContactRequest(2L, 7L, ContactRequestStatus.REJECTED, patientAccountId = 21L)
        val state = ClinicianAgendaUiState(
            requests = listOf(open, rejected), selectedRequestId = 2L,
            agenda = listOf(appointment(5, AppointmentStatus.REQUESTED), appointment(6, AppointmentStatus.HELD)),
            slots = listOf(BookableSlot(now, "x", SlotAvailability.FREE), BookableSlot(now, "y", SlotAvailability.HELD))
        )
        assertEquals(listOf(open), state.openRequests)
        assertNull(state.selectedRequest)
        assertEquals(5L, state.proposalFor(1L)?.id)
        assertEquals(1, state.freeSlots)
        assertEquals("Paciente #20", patientLabel(open.patientAccountId))
    }

    @Test fun clinicianProfileIsMappedWithoutInventedRatings() {
        val professional = ClinicianProfileDto(5L, "Dra. Laura Gómez", "Psicóloga clínica", "Bio", listOf("Ansiedad"),
            ConsultationRateDto(120.0, "PEN", 50)).toProfessional(rating = null)
        assertEquals("5", professional.id)
        assertEquals("LG", professional.initials)
        assertEquals("S/ 120", professional.priceLabel)
        assertNull(professional.rating)
        assertEquals(0, professional.reviewCount)
    }

    @Test fun invalidRoutineIsNotAddedAndCompletionIsIdempotent() {
        val viewModel = RutinesViewModel()
        val count = viewModel.uiState.value.routines.size
        assertFalse(viewModel.add(" ", "", "09:00", 5, setOf(0), true))
        assertFalse(viewModel.add("Pausa", "", "25:00", 5, setOf(0), true))
        assertFalse(viewModel.add("Pausa", "", "09:00", 0, emptySet(), true))
        assertEquals(count, viewModel.uiState.value.routines.size)
        assertTrue(viewModel.add("Pausa", "Respirar", "10:30", 3, setOf(0, 2), false))
        viewModel.complete("morning")
        viewModel.complete("morning")
        assertEquals(1, viewModel.uiState.value.routines.count { it.completed })
    }
}
