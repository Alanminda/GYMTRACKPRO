/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/data/remote/dto/ExerciseDtos.kt
 * Proposito: Modelos DTO para serializacion/deserializacion de API.
 */
package com.example.gymtrackpro.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ExerciseDto(
    // Id estilo Mongo retornado por algunas variantes del backend.
    @SerializedName("_id")
    val mongoId: String? = null,
    // Id alterno de ejercicio (fuentes externas distintas).
    val id: String? = null,
    // Nombre visible del ejercicio.
    val name: String,
    // URL de imagen/gif.
    val gifUrl: String? = null,
    // Campos de clasificacion para filtros en UI.
    val muscleGroup: String? = null,
    val bodyPart: String? = null,
    val equipment: String? = null,
    val target: String? = null,
    // Metadatos para detalle.
    val secondaryMuscles: List<String>? = null,
    val instructions: List<String>? = null
)

