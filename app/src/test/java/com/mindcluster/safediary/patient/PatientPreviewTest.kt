package com.mindcluster.safediary.patient

import com.mindcluster.safediary.professionals.presentation.viewmodels.ProfessionalsViewModel
import com.mindcluster.safediary.rutines.presentation.viewmodels.RutinesViewModel
import com.mindcluster.safediary.scheduling.domain.model.AppointmentStatus
import com.mindcluster.safediary.scheduling.presentation.viewmodels.SchedulingViewModel
import org.junit.Assert.*
import org.junit.Test

class PatientPreviewTest {
    @Test fun expiredReservationCannotBeConfirmedByPayment() {
        val viewModel = SchedulingViewModel()
        val reservation = viewModel.uiState.value.appointments.first { it.status == AppointmentStatus.RESERVED }
        viewModel.tick(reservation.expiresAt!!)
        assertFalse(viewModel.simulatePayment(reservation.id))
        assertEquals(AppointmentStatus.EXPIRED, viewModel.uiState.value.appointments.first { it.id == reservation.id }.status)
    }

    @Test fun acceptingSameProposalDoesNotCreateDuplicateActiveReservations() {
        val viewModel = SchedulingViewModel()
        assertEquals("reservation", viewModel.reserve("laura"))
        viewModel.cancel("reservation")
        val replacementId = viewModel.reserve("laura")!!
        assertNotEquals("reservation", replacementId)
        assertEquals(replacementId, viewModel.reserve("laura"))
        assertEquals(1, viewModel.uiState.value.appointments.count { it.conversationId == "laura" && it.status == AppointmentStatus.RESERVED })
    }

    @Test fun onlyCompletedAppointmentsCanBeRatedOnce() {
        val viewModel = SchedulingViewModel()
        viewModel.rate("reservation", 5, "No hubo sesión")
        assertNull(viewModel.uiState.value.appointments.first { it.id == "reservation" }.rating)
        viewModel.rate("completed", 4, "Me sentí escuchada")
        viewModel.rate("completed", 1, "Segunda reseña")
        assertEquals(4, viewModel.uiState.value.appointments.first { it.id == "completed" }.rating)
    }

    @Test fun callAccessRequiresConfirmedAppointmentAndItsTimeWindow() {
        val viewModel = SchedulingViewModel()
        val state = viewModel.uiState.value
        val confirmed = state.appointments.first { it.status == AppointmentStatus.CONFIRMED }
        assertTrue(confirmed.canJoin(state.now))
        assertFalse(confirmed.canJoin(confirmed.startsAt - 11 * 60_000))
        assertFalse(confirmed.canJoin(confirmed.startsAt + 51 * 60_000))
        assertFalse(state.appointments.first { it.status == AppointmentStatus.RESERVED }.canJoin(state.now))
    }

    @Test fun searchIgnoresAccentsAndFiltersCompose() {
        val viewModel = ProfessionalsViewModel()
        viewModel.search("gomez")
        assertEquals("laura", viewModel.uiState.value.results.single().id)
        viewModel.affordable(true)
        assertTrue(viewModel.uiState.value.results.isEmpty())
        viewModel.clearFilters()
        viewModel.toggleFavorite("liam")
        viewModel.favoritesOnly(true)
        assertEquals("liam", viewModel.uiState.value.results.single().id)
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
