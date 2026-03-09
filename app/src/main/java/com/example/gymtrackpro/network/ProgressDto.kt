/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/network/ProgressDto.kt
 * Proposito: Capa de red legacy mantenida por compatibilidad.
 */
package com.example.gymtrackpro.network

data class ProgressDto(
    // Id opcional cuando el backend ya lo asigno.
    val id: String? = null,
    // Fecha de registro de progreso.
    val date: String,
    // Peso registrado.
    val weight: Double,
    // Usuario propietario del registro.
    val userId: Int
)

