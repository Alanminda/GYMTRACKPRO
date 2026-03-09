/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/data/remote/dto/ProgressDtos.kt
 * Proposito: Modelos DTO para serializacion/deserializacion de API.
 */
package com.example.gymtrackpro.data.remote.dto

data class ProgressUpsertRequest(
    // Fecha del registro de progreso (YYYY-MM-DD).
    val dateIso: String,
    // Peso registrado en la fecha indicada.
    val weight: Double,
    // Nota opcional del usuario.
    val note: String?
)

