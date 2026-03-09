/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/data/remote/dto/ProfileDtos.kt
 * Proposito: Modelos DTO para serializacion/deserializacion de API.
 */
package com.example.gymtrackpro.data.remote.dto

data class ProfileDto(
    // Id remoto de usuario.
    val id: String,
    // Datos mostrados/editar en pantalla Profile.
    val name: String,
    val email: String,
    // Fecha de alta del usuario (opcional).
    val createdAt: String?
)

// [Req B] Payload para actualizar perfil.
data class ProfileUpdateRequest(
    val name: String,
    val email: String
)

