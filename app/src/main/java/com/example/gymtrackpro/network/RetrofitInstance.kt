/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/network/RetrofitInstance.kt
 * Proposito: Capa de red legacy mantenida por compatibilidad.
 */
package com.example.gymtrackpro.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // [Req B] Cliente Retrofit legacy para pruebas locales (emulador -> localhost).
    private const val BASE_URL = "http://10.0.2.2:3000/" // 10.0.2.2 es el localhost para el emulador Android

    // Instancia singleton para reutilizar conexiones HTTP.
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

