package com.example.gymtrackpro.data.local.entities

import androidx.room.Entity

@Entity(
    tableName = "routine_exercise",
    primaryKeys = ["routineId", "exerciseId"]
)
data class RoutineExerciseEntity(
    val routineId: Int,
    val exerciseId: String,
    val isCompleted: Boolean = false,
    val syncState: String = "PENDING_UPSERT",
    val deleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
