package com.mindcluster.safediary.rutines.infrastructure.remote

import android.util.Log
import com.mindcluster.safediary.rutines.infrastructure.remote.api.RutinesApiService
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.CreateDailyRoutineRequestDto
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.CreatePromptReflectionRequestDto
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.DailyRoutineResponseDto
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.PromptReflectionResponseDto
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.SosExerciseResponseDto
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.StartSosExerciseRequestDto
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.SubmitDailyReflectionRequestDto
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.ToggleRoutineNotificationRequestDto
import com.mindcluster.safediary.rutines.infrastructure.remote.dto.UpdateDailyRoutineRequestDto
import com.mindcluster.safediary.shared.infrastructure.network.RetrofitClientProvider

class RetrofitRutinesGateway(
    private val apiService: RutinesApiService = RetrofitClientProvider.createSessionService(com.mindcluster.safediary.shared.infrastructure.security.AuthSessionHolder.token)
) {
    private val tag = "RutinesGateway"

    suspend fun deleteRoutine(id: Long): Boolean = try {
        apiService.deleteRoutine(id).isSuccessful
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) { false }

    suspend fun completeRoutine(id: Long): DailyRoutineResponseDto? = try {
        apiService.completeRoutine(id).takeIf { it.isSuccessful }?.body()
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) { null }


    suspend fun createDailyRoutine(
        patientId: Long,
        title: String,
        frequencyDays: Set<String>,
        isNotificationActive: Boolean,
        description: String = "", time: String = "09:00", minutes: Int = 5, kind: String = "GROUNDING"
    ): DailyRoutineResponseDto? = try {
        val request = CreateDailyRoutineRequestDto(patientId, title, frequencyDays, isNotificationActive, description, time, minutes, kind)
        val response = apiService.createDailyRoutine(request)
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "createDailyRoutine error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "createDailyRoutine network error: ${e.message}")
        null
    }

    suspend fun getDailyRoutineById(id: Long): DailyRoutineResponseDto? = try {
        val response = apiService.getDailyRoutineById(id)
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "getDailyRoutineById error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "getDailyRoutineById network error: ${e.message}")
        null
    }

    suspend fun getRoutinesByPatientId(patientId: Long): List<DailyRoutineResponseDto>? = try {
        val response = apiService.getAllRoutinesByPatientId(patientId)
        if (response.isSuccessful) {
            response.body().orEmpty()
        } else {
            Log.w(tag, "getRoutinesByPatientId error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "getRoutinesByPatientId network error: ${e.message}")
        null
    }

    suspend fun updateDailyRoutine(
        id: Long,
        title: String,
        isNotificationActive: Boolean,
        frequencyDays: Set<String>? = null, description: String? = null,
        time: String? = null, minutes: Int? = null, kind: String? = null
    ): DailyRoutineResponseDto? = try {
        val request = UpdateDailyRoutineRequestDto(title, isNotificationActive, frequencyDays, description, time, minutes, kind)
        val response = apiService.updateDailyRoutine(id, request)
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "updateDailyRoutine error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "updateDailyRoutine network error: ${e.message}")
        null
    }

    suspend fun toggleRoutineNotification(
        id: Long,
        isEnabled: Boolean
    ): DailyRoutineResponseDto? = try {
        val request = ToggleRoutineNotificationRequestDto(isEnabled)
        val response = apiService.toggleRoutineNotification(id, request)
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "toggleRoutineNotification error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "toggleRoutineNotification network error: ${e.message}")
        null
    }

    suspend fun toggleRoutineActive(id: Long): DailyRoutineResponseDto? = try {
        val response = apiService.toggleRoutineActive(id)
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "toggleRoutineActive error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "toggleRoutineActive network error: ${e.message}")
        null
    }

    suspend fun createPromptReflection(
        patientId: Long,
        promptText: String
    ): PromptReflectionResponseDto? = try {
        val request = CreatePromptReflectionRequestDto(patientId, promptText)
        val response = apiService.createPromptReflection(request)
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "createPromptReflection error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "createPromptReflection network error: ${e.message}")
        null
    }

    suspend fun getPromptReflectionById(id: Long): PromptReflectionResponseDto? = try {
        val response = apiService.getPromptReflectionById(id)
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "getPromptReflectionById error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "getPromptReflectionById network error: ${e.message}")
        null
    }

    suspend fun getPendingPromptReflection(patientId: Long): PromptReflectionResponseDto? = try {
        val response = apiService.getPendingPromptReflection(patientId)
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "getPendingPromptReflection error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "getPendingPromptReflection network error: ${e.message}")
        null
    }

    suspend fun submitDailyReflection(
        id: Long,
        answer: String
    ): PromptReflectionResponseDto? = try {
        val request = SubmitDailyReflectionRequestDto(answer)
        val response = apiService.submitDailyReflection(id, request)
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "submitDailyReflection error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "submitDailyReflection network error: ${e.message}")
        null
    }

    suspend fun startSosExercise(
        patientId: Long,
        exerciseType: String,
        totalSteps: Int
    ): SosExerciseResponseDto? = try {
        val request = StartSosExerciseRequestDto(patientId, exerciseType, totalSteps)
        val response = apiService.startSosExercise(request)
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "startSosExercise error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "startSosExercise network error: ${e.message}")
        null
    }

    suspend fun getSosExerciseById(id: Long): SosExerciseResponseDto? = try {
        val response = apiService.getSosExerciseById(id)
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "getSosExerciseById error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "getSosExerciseById network error: ${e.message}")
        null
    }

    suspend fun getSosExerciseHistory(patientId: Long): List<SosExerciseResponseDto> = try {
        val response = apiService.getSosExerciseHistory(patientId)
        if (response.isSuccessful) {
            response.body().orEmpty()
        } else {
            Log.w(tag, "getSosExerciseHistory error ${response.code()}: ${response.message()}")
            emptyList()
        }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "getSosExerciseHistory network error: ${e.message}")
        emptyList()
    }

    suspend fun advanceSosStep(id: Long): SosExerciseResponseDto? = try {
        val response = apiService.advanceSosStep(id)
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "advanceSosStep error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "advanceSosStep network error: ${e.message}")
        null
    }

    suspend fun completeSosExercise(id: Long): SosExerciseResponseDto? = try {
        val response = apiService.completeSosExercise(id)
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "completeSosExercise error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "completeSosExercise network error: ${e.message}")
        null
    }
}
