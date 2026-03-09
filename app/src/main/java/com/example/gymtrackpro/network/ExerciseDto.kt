/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/network/ExerciseDto.kt
 * Proposito: Capa de red legacy mantenida por compatibilidad.
 */
package com.example.gymtrackpro.network

data class ExerciseDto(
    // Id remoto del ejercicio.
    val id: String,
    // Nombre para render en lista/tarjeta.
    val name: String,
    // Grupo muscular principal para filtro/etiqueta.
    val muscleGroup: String
)

