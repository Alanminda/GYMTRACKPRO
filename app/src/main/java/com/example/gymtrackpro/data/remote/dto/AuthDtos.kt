/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/data/remote/dto/AuthDtos.kt
 * Proposito: Modelos DTO para serializacion/deserializacion de API.
 */
package com.example.gymtrackpro.data.remote.dto

// [Req B] Payload de registro enviado al backend.
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

// [Req B] Payload de login.
data class LoginRequest(
    val email: String,
    val password: String
)

// [Req B] Respuesta de autenticacion (token + datos basicos de usuario).
data class AuthResponse(
    val token: String,
    val name: String,
    val email: String
)

