package com.mindcluster.safediary.profiles.infrastructure.remote

import android.util.Log
import com.google.gson.Gson
import com.mindcluster.safediary.profiles.infrastructure.remote.api.ProfilesApiService
import com.mindcluster.safediary.profiles.infrastructure.remote.dto.*
import com.mindcluster.safediary.shared.infrastructure.network.ApiEndpoints
import com.mindcluster.safediary.shared.infrastructure.network.RetrofitClientProvider

class RetrofitPatientProfileGateway(
    private val apiService: ProfilesApiService = RetrofitClientProvider.createService()
) {
    private val tag = "PatientProfileGateway"
    private val gson = Gson()

    /** Profile of the signed-in patient, resolved by the backend from the access token. */
    suspend fun fetchCurrentPatientProfile(): PatientResponseDto? = try {
        val response = apiService.getPatientById(ApiEndpoints.currentPatient)
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "fetchCurrentPatientProfile returned error code ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: Exception) {
        Log.w(tag, "fetchCurrentPatientProfile network failure: ${e.message}")
        null
    }

    suspend fun fetchPatientProfile(patientId: Long): PatientResponseDto? = try {
        val response = apiService.getPatientById(ApiEndpoints.patientById(patientId))
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "fetchPatientProfile returned error code ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: Exception) {
        Log.w(tag, "fetchPatientProfile network failure: ${e.message}")
        null
    }

    suspend fun updateEmergencyContact(
        patientId: Long,
        request: UpdateEmergencyContactRequestDto
    ): PatientResponseDto? = try {
        val response = apiService.updateEmergencyContact(ApiEndpoints.patientEmergencyContact(patientId), request)
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "updateEmergencyContact returned error code ${response.code()}")
            null
        }
    } catch (e: Exception) {
        Log.w(tag, "updateEmergencyContact network failure: ${e.message}")
        null
    }

    suspend fun deleteEmergencyContact(patientId: Long): PatientResponseDto? = try {
        val response = apiService.deleteEmergencyContact(ApiEndpoints.patientEmergencyContact(patientId))
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "deleteEmergencyContact returned error code ${response.code()}")
            null
        }
    } catch (e: Exception) {
        Log.w(tag, "deleteEmergencyContact network failure: ${e.message}")
        null
    }

    suspend fun updatePatientProfile(
        patientId: Long,
        firstName: String,
        lastName: String
    ): PatientResponseDto? = try {
        val response = apiService.updatePatientProfile(
            ApiEndpoints.patientById(patientId),
            UpdatePatientProfileRequestDto(firstName, lastName)
        )
        if (response.isSuccessful) {
            response.body()
        } else {
            Log.w(tag, "updatePatientProfile returned error code ${response.code()}")
            null
        }
    } catch (e: Exception) {
        Log.w(tag, "updatePatientProfile network failure: ${e.message}")
        null
    }

    suspend fun updatePatientEmail(
        patientId: Long,
        newEmail: String,
        currentPassword: String
    ): Result<PatientResponseDto> = try {
        val response = apiService.updatePatientEmail(
            ApiEndpoints.patientEmail(patientId),
            UpdatePatientEmailRequestDto(newEmail, currentPassword)
        )
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            val errorBodyStr = response.errorBody()?.string()
            val errorMessage = try {
                val errorDto = gson.fromJson(errorBodyStr, ApiErrorResponseDto::class.java)
                errorDto?.details ?: errorDto?.message ?: "Error al actualizar el correo electrónico (${response.code()})"
            } catch (_: Exception) {
                "Error al actualizar el correo electrónico (${response.code()})"
            }
            Result.failure(IllegalArgumentException(errorMessage))
        }
    } catch (e: Exception) {
        Log.w(tag, "updatePatientEmail network failure: ${e.message}")
        Result.failure(e)
    }
}
