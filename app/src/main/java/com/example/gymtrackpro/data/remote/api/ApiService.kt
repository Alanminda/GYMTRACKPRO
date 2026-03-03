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
    suspend fun getExercises(@Header("Authorization") bearer: String): List<ExerciseDto>

    @POST("progress")
    suspend fun sendProgress(
        @Header("Authorization") bearer: String,
        @Body body: ProgressUpsertRequest
    ): Response<Unit>
}
