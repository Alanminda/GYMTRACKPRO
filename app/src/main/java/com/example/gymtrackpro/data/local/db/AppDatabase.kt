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
    abstract fun userLocalDao(): UserLocalDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun progressDao(): ProgressDao
    abstract fun routineDao(): RoutineDao
    abstract fun routineExerciseDao(): RoutineExerciseDao
}
