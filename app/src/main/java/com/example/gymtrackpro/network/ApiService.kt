package com.example.gymtrackpro.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("exercises")
    suspend fun getExercises(): List<ExerciseDto>

    @POST("progress")
    suspend fun sendProgress(@Body progress: ProgressDto): Response<Unit>
}
