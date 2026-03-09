/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/data/remote/api/ApiService.kt
 * Proposito: Define cliente y contratos Retrofit para backend remoto.
 */
package com.example.gymtrackpro.data.remote.api

import com.example.gymtrackpro.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ===== Auth =====
    // [Req B] Auth: envio de datos hacia API (registro/login).
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    // ===== Perfil =====
    @GET("me")
    suspend fun getProfile(@Header("Authorization") bearer: String): ProfileDto

    @PUT("me")
    suspend fun updateProfile(
        @Header("Authorization") bearer: String,
        @Body body: ProfileUpdateRequest
    ): ProfileDto

    // ===== Ejercicios =====
    // [Req B] Listar + filtrar ejercicios por paginacion y query.
    @GET("exercises")
    suspend fun getExercises(
        @Header("Authorization") bearer: String?,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("q") q: String? = null
    ): List<ExerciseDto>

    // [Req B] Endpoint publico para modo invitado online.
    @GET("public/exercises")
    suspend fun getPublicExercises(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("q") q: String? = null
    ): List<ExerciseDto>

    // [Req B] Detalle de ejercicio (enriquecimiento de tarjeta/lista).
    @GET("exercises/{id}")
    suspend fun getExerciseById(
        @Header("Authorization") bearer: String?,
        @Path("id") id: String
    ): ExerciseDto

    @GET("public/exercises/{id}")
    suspend fun getPublicExerciseById(
        @Path("id") id: String
    ): ExerciseDto

    // ===== Rutinas privadas =====
    @GET("routines")
    suspend fun getRoutines(@Header("Authorization") bearer: String): List<RoutineRemoteDto>

    @POST("routines")
    suspend fun createRoutine(
        @Header("Authorization") bearer: String,
        @Body body: RoutineUpsertRequest
    ): RoutineRemoteDto

    @PUT("routines/{id}")
    suspend fun updateRoutine(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: RoutineUpsertRequest
    ): RoutineRemoteDto

    @HTTP(method = "DELETE", path = "routines/{id}", hasBody = false)
    suspend fun deleteRoutine(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): Response<Unit>

    @PUT("routines/{id}/exercises")
    suspend fun syncRoutineExercises(
        @Header("Authorization") bearer: String,
        @Path("id") id: String,
        @Body body: RoutineExerciseSyncRequest
    ): Response<Unit>

    @POST("routines/{id}/share")
    suspend fun shareRoutine(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): CommunityRoutineDto

    // ===== Comunidad =====
    // [Req B] Comunidad: lista y filtros (sort, q, paginacion).
    @GET("community/routines")
    suspend fun getCommunityRoutines(
        @Header("Authorization") bearer: String?,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("q") q: String? = null,
        @Query("sort") sort: String? = null
    ): List<CommunityRoutineDto>

    @GET("public/community/routines")
    suspend fun getPublicCommunityRoutines(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("q") q: String? = null,
        @Query("sort") sort: String? = null
    ): List<CommunityRoutineDto>

    // [Req B] Accion sobre API (favoritos).
    @POST("community/routines/{id}/favorite")
    suspend fun favoriteCommunityRoutine(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): Response<CommunityFavoriteToggleDto>

    @HTTP(method = "DELETE", path = "community/routines/{id}/favorite", hasBody = false)
    suspend fun unfavoriteCommunityRoutine(
        @Header("Authorization") bearer: String,
        @Path("id") id: String
    ): Response<CommunityFavoriteToggleDto>

    @GET("community/favorites")
    suspend fun getCommunityFavorites(@Header("Authorization") bearer: String): List<CommunityRoutineDto>

    // ===== Progreso =====
    @POST("progress")
    suspend fun sendProgress(
        @Header("Authorization") bearer: String,
        @Body body: ProgressUpsertRequest
    ): Response<Unit>
}

