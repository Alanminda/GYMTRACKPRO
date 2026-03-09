/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/data/remote/api/ApiClient.kt
 * Proposito: Define cliente y contratos Retrofit para backend remoto.
 */
package com.example.gymtrackpro.data.remote.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // [Req B] Base URL del backend REST (Railway).
    const val BASE_URL = "https://gymtrackpro-production.up.railway.app/"

    // [Req B] Cliente Retrofit + OkHttp para consumo de API externa/propia.
    val api: ApiService by lazy {
        val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logger).build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

