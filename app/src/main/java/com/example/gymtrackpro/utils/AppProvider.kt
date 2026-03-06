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
        val appContext = context.applicationContext
        val database = db ?: synchronized(this) {
            db ?: Room.databaseBuilder(appContext, AppDatabase::class.java, "gymtrack.db")
                .fallbackToDestructiveMigration()
                .build()
                .also { db = it }
        }

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
