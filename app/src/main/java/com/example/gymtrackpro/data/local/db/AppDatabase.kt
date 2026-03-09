/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/data/local/db/AppDatabase.kt
 * Proposito: Configura la base de datos local Room y expone DAOs.
 */
package com.example.gymtrackpro.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gymtrackpro.data.local.dao.ExerciseDao
import com.example.gymtrackpro.data.local.dao.ProgressDao
import com.example.gymtrackpro.data.local.dao.RoutineDao
import com.example.gymtrackpro.data.local.dao.RoutineExerciseDao
import com.example.gymtrackpro.data.local.dao.UserLocalDao
import com.example.gymtrackpro.data.local.entities.*

@Database(
    // [Req A] Room Database local con 5 entidades principales relacionadas.
    // Entidades:
    // 1) UserLocalEntity
    // 2) RoutineEntity
    // 3) ExerciseEntity
    // 4) RoutineExerciseEntity (tabla puente N:M rutina-ejercicio)
    // 5) ProgressEntity
    entities = [
        UserLocalEntity::class,
        RoutineEntity::class,
        ExerciseEntity::class,
        RoutineExerciseEntity::class,
        ProgressEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // [Req A] DAOs expuestos para CRUD y consultas reactivas.
    abstract fun userLocalDao(): UserLocalDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun progressDao(): ProgressDao
    abstract fun routineDao(): RoutineDao
    abstract fun routineExerciseDao(): RoutineExerciseDao
}

