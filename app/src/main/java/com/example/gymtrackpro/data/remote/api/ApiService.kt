package com.example.gymtrackpro.data.remote.api

import com.example.gymtrackpro.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("exercises")
    suspend fun getExercises(
        @Header("Authorization") bearer: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("q") q: String? = null
    ): List<ExerciseDto>

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

    @POST("progress")
    suspend fun sendProgress(
        @Header("Authorization") bearer: String,
        @Body body: ProgressUpsertRequest
    ): Response<Unit>
}
