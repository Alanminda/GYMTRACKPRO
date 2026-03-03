package com.example.gymtrackpro.data.remote.dto

data class ProgressUpsertRequest(
    val dateIso: String,
    val weight: Double,
    val note: String?
)
