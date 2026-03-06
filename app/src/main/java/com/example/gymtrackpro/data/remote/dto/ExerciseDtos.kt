package com.example.gymtrackpro.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ExerciseDto(
    @SerializedName("_id")
    val mongoId: String? = null,
    val id: String? = null,
    val name: String,
    val muscleGroup: String? = null,
    val bodyPart: String? = null,
    val equipment: String? = null,
    val target: String? = null
)
