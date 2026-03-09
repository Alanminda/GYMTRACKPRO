/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/utils/AppProvider.kt
 * Proposito: Utilidades de inyeccion y fabrica de ViewModels.
 */
package com.example.gymtrackpro.utils

import android.content.Context
import androidx.room.Room
import com.example.gymtrackpro.data.local.db.AppDatabase
import com.example.gymtrackpro.data.remote.api.ApiClient
import com.example.gymtrackpro.data.repository.GymRepository

object AppProvider {

    @Volatile private var db: AppDatabase? = null
    @Volatile private var repo: GymRepository? = null

    fun provideRepository(context: Context): GymRepository {
        // [Req A] Inicializa Room como fuente local principal (offline-first).
        val appContext = context.applicationContext
        val database = db ?: synchronized(this) {
            db ?: Room.databaseBuilder(appContext, AppDatabase::class.java, "gymtrack.db")
                .fallbackToDestructiveMigration()
                .build()
                .also { db = it }
        }

        // [Req C - Repository Pattern] Inyecta DAOs (local) + Retrofit API (remota).
        return repo ?: synchronized(this) {
            repo ?: GymRepository(
                userDao = database.userLocalDao(),
                exerciseDao = database.exerciseDao(),
                progressDao = database.progressDao(),
                routineDao = database.routineDao(),
                routineExerciseDao = database.routineExerciseDao(),
                api = ApiClient.api
            ).also { repo = it }
        }
    }
}

