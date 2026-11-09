package com.mindcluster.safediary.patient

import com.mindcluster.safediary.rutines.domain.model.RoutineKind
import com.mindcluster.safediary.rutines.infrastructure.remote.RetrofitRutinesGateway
import com.mindcluster.safediary.rutines.infrastructure.remote.api.RutinesApiService
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.*
import com.mindcluster.safediary.rutines.presentation.viewmodels.RutinesViewModel
import com.mindcluster.safediary.rutines.presentation.viewmodels.toRoutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.lang.reflect.Proxy

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineManagementTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun cleanup() { Dispatchers.resetMain() }
    private val rows = mutableMapOf<Long, DailyRoutineResponseDto>()
    private var failDelete = false
    private var failSave = false
    private var completions = 0
    private val api = Proxy.newProxyInstance(RutinesApiService::class.java.classLoader, arrayOf(RutinesApiService::class.java)) { _, method, args ->
        when (method.name) {
            "getAllRoutinesByPatientId" -> Response.success(rows.values.filter { it.patientId == args!![0] })
            "createDailyRoutine" -> {
                if (failSave) Response.success<DailyRoutineResponseDto>(null) else {
                    val request = args!![0] as CreateDailyRoutineRequestDto
                    val id = (rows.keys.maxOrNull() ?: 0) + 1
                    val row = DailyRoutineResponseDto(id, request.patientId, request.title, request.frequencyDays,
                        if (request.isNotificationActive) "ENABLED" else "DISABLED", true,
                        request.description, request.time, request.minutes, request.kind)
                    rows[id] = row
                    Response.success(row)
                }
            }
            "updateDailyRoutine" -> {
                val id = args!![0] as Long; val request = args[1] as UpdateDailyRoutineRequestDto
                val row = rows.getValue(id).copy(title = request.title, frequencyDays = request.frequencyDays!!,
                    description = request.description, time = request.time, minutes = request.minutes, kind = request.kind,
                    notificationStatus = if (request.isNotificationActive) "ENABLED" else "DISABLED")
                rows[id] = row; Response.success(row)
            }
            "deleteRoutine" -> if (failDelete) Response.error<Unit>(503, "{}".toResponseBody()) else {
                rows.remove(args!![0]); Response.success(Unit)
            }
            "completeRoutine" -> {
                val id = args!![0] as Long; completions++
                val row = rows.getValue(id).copy(completedOn = "2026-11-09")
                rows[id] = row; Response.success(row)
            }
            else -> throw UnsupportedOperationException(method.name)
        }
    } as RutinesApiService
    private fun vm(account: Long = 1) = RutinesViewModel(RetrofitRutinesGateway(api), account)

    @Test fun emptyServerDoesNotInjectSharedExamples() = runTest(dispatcher) {
        val vm = vm(); vm.loadRoutines(); runCurrent()
        assertTrue(vm.uiState.value.routines.isEmpty())
        assertNull(vm.uiState.value.error)
    }

    @Test fun createdAndEditedDetailsSurviveReloadAndDeletionStaysDeleted() = runTest(dispatcher) {
        val vm = vm()
        assertTrue(vm.add("Mi pausa", "Respirar", "18:30", 7, setOf(0, 4), false, RoutineKind.BREATHING))
        runCurrent()
        val id = vm.uiState.value.routines.single().id
        val reopened = vm(); reopened.loadRoutines(); runCurrent()
        with(reopened.uiState.value.routines.single()) {
            assertEquals("18:30", time); assertEquals(7, minutes); assertEquals("Respirar", description)
            assertEquals(setOf(0, 4), days); assertEquals(RoutineKind.BREATHING, kind); assertFalse(reminder)
        }
        reopened.save(id, "Editada", "Sentidos", "20:15", 3, setOf(6), true, RoutineKind.GROUNDING) {}
        runCurrent(); reopened.loadRoutines(); runCurrent()
        with(reopened.uiState.value.routines.single()) {
            assertEquals("Editada", title); assertEquals("Sentidos", description); assertEquals("20:15", time)
            assertEquals(3, minutes); assertEquals(setOf(6), days); assertEquals(RoutineKind.GROUNDING, kind); assertTrue(reminder)
        }
        reopened.delete(id); runCurrent(); reopened.loadRoutines(); runCurrent()
        assertTrue(reopened.uiState.value.routines.isEmpty()); assertTrue(rows.isEmpty())
    }

    @Test fun newAccountDoesNotSeePreviousAccountRoutines() = runTest(dispatcher) {
        val a = vm(); a.add("Solo A", "", "09:00", 5, setOf(0), true); runCurrent()
        val b = vm(2); b.loadRoutines(); runCurrent()
        assertTrue(b.uiState.value.routines.isEmpty()); assertEquals("Solo A", a.uiState.value.routines.single().title)
    }

    @Test fun failedSaveDoesNotCreateARoutineAndFailedDeleteKeepsExistingOne() = runTest(dispatcher) {
        val vm = vm(); failSave = true
        vm.add("Pausa", "", "09:00", 5, setOf(0), true); runCurrent()
        assertTrue(vm.uiState.value.routines.isEmpty()); assertNotNull(vm.uiState.value.error)
        failSave = false; vm.add("Pausa", "", "09:00", 5, setOf(0), true); runCurrent()
        failDelete = true; vm.delete(vm.uiState.value.routines.single().id); runCurrent()
        assertEquals(1, vm.uiState.value.routines.size); assertNotNull(vm.uiState.value.error)
    }

    @Test fun completionIsForOneLimaDayAndDoesNotDependOnActiveFlag() {
        val row = DailyRoutineResponseDto(1, 1, "Pausa", setOf("MONDAY"), "ENABLED", false, completedOn = "2026-11-09")
        assertTrue(row.toRoutine("2026-11-09").completed)
        assertFalse(row.toRoutine("2026-11-10").completed)
        assertFalse(row.copy(completedOn = null).toRoutine("2026-11-09").completed)
    }
}
