package com.mindcluster.safediary.professionals.infrastructure.remote

import android.util.Log
import com.mindcluster.safediary.professionals.domain.model.Professional
import com.mindcluster.safediary.professionals.infrastructure.remote.api.ClinicianDirectoryApiService
import com.mindcluster.safediary.professionals.infrastructure.remote.dto.ClinicianProfileDto
import com.mindcluster.safediary.professionals.infrastructure.remote.dto.RatingSummaryDto
import com.mindcluster.safediary.shared.infrastructure.network.RetrofitClientProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.Locale

class RetrofitClinicianDirectoryGateway(
    private val apiService: ClinicianDirectoryApiService = RetrofitClientProvider.createService()
) {
    private val tag = "ClinicianDirectoryGateway"

    /** Verified, published clinicians with their rating, or null when the directory cannot be reached. */
    suspend fun searchClinicians(): List<Professional>? = try {
        val response = apiService.searchClinicians()
        if (response.isSuccessful) {
            coroutineScope {
                response.body().orEmpty().map { dto -> async { dto.toProfessional(getRating(dto.id)) } }.awaitAll()
            }
        } else {
            Log.w(tag, "searchClinicians error ${response.code()}: ${response.message()}")
            null
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(tag, "searchClinicians network error: ${e.message}")
        null
    }

    private suspend fun getRating(id: Long): RatingSummaryDto? = try {
        apiService.getRating(id).takeIf { it.isSuccessful }?.body()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
}

internal fun ClinicianProfileDto.toProfessional(rating: RatingSummaryDto?): Professional {
    val name = displayName?.trim().orEmpty().ifBlank { "Psicólogo" }
    return Professional(
        id = id.toString(),
        name = name,
        initials = initialsOf(name),
        specialty = professionalTitle.orEmpty(),
        tags = specialties.orEmpty(),
        approach = bio.orEmpty(),
        price = rate?.amount?.toInt() ?: 0,
        currency = rate?.currency ?: "PEN",
        durationMinutes = rate?.durationMinutes ?: 50,
        rating = rating?.average?.let { String.format(Locale.US, "%.1f", it) },
        reviewCount = rating?.reviewCount ?: 0
    )
}

/** Initials from the name, skipping honorifics such as "Dra." or "Ps.". */
internal fun initialsOf(name: String): String =
    name.split(" ").filter { it.isNotBlank() && !it.endsWith(".") }
        .take(2).joinToString("") { it.first().uppercase() }
        .ifBlank { "PS" }
