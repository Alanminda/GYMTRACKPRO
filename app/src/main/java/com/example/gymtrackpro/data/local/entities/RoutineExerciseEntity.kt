package com.example.gymtrackpro.data.local.entities

import androidx.room.Entity

@Entity(
    tableName = "routine_exercise",
    primaryKeys = ["routineId", "exerciseId"]
)
data class RoutineExerciseEntity(
    val routineId: Int,
    val exerciseId: String
)
