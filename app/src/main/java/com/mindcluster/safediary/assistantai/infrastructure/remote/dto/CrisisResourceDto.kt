package com.mindcluster.safediary.assistantai.infrastructure.remote.dto

import com.google.gson.annotations.SerializedName

data class CrisisResourceDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("description")
    val description: String? = null
)
