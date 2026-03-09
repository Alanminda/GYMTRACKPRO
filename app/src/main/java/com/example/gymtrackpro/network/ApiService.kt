/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/network/ApiService.kt
 * Proposito: Capa de red legacy mantenida por compatibilidad.
 */
package com.example.gymtrackpro.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    // [Req B] Listar datos desde API (legacy/local).
    @GET("exercises")
    suspend fun getExercises(): List<ExerciseDto>

    // [Req B] Enviar datos hacia API (POST).
    @POST("progress")
    suspend fun sendProgress(@Body progress: ProgressDto): Response<Unit>
}

