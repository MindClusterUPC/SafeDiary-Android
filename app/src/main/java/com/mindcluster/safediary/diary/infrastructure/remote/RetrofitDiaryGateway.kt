package com.mindcluster.safediary.diary.infrastructure.remote

import com.mindcluster.safediary.diary.infrastructure.remote.api.DiaryApiService
import com.mindcluster.safediary.diary.infrastructure.remote.dto.CreateDiaryEntryRequest
import com.mindcluster.safediary.diary.infrastructure.remote.dto.DiaryEntryDto
import com.mindcluster.safediary.diary.infrastructure.remote.dto.UpdateDiaryEntryRequest
import com.mindcluster.safediary.shared.infrastructure.network.RetrofitClientProvider

class RetrofitDiaryGateway(
    private val api: DiaryApiService = RetrofitClientProvider.createService(DiaryApiService::class.java)
) {
    suspend fun listEntries(patientId: Long): Result<List<DiaryEntryDto>> = runCatching {
        val response = api.listEntries(patientId)
        if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else {
            throw Exception("HTTP ${response.code()}: ${response.message()}")
        }
    }

    suspend fun createEntry(patientId: Long, title: String, content: String, vaulted: Boolean): Result<DiaryEntryDto> = runCatching {
        val response = api.createEntry(CreateDiaryEntryRequest(patientId, title, content, vaulted))
        if (response.isSuccessful && response.body() != null) {
            response.body()!!
        } else {
            throw Exception("HTTP ${response.code()}: ${response.message()}")
        }
    }

    suspend fun updateEntry(remoteId: Long, title: String, content: String): Result<DiaryEntryDto> = runCatching {
        val response = api.updateEntry(remoteId, UpdateDiaryEntryRequest(title, content))
        if (response.isSuccessful && response.body() != null) {
            response.body()!!
        } else {
            throw Exception("HTTP ${response.code()}: ${response.message()}")
        }
    }

    suspend fun deleteEntry(remoteId: Long): Result<Unit> = runCatching {
        val response = api.deleteEntry(remoteId)
        if (!response.isSuccessful && response.code() != 404) {
            throw Exception("HTTP ${response.code()}: ${response.message()}")
        }
    }
}
