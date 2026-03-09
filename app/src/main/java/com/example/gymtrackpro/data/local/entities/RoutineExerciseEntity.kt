/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/data/local/entities/RoutineExerciseEntity.kt
 * Proposito: Define entidad local de Room para persistencia offline.
 */
package com.example.gymtrackpro.data.local.entities

import androidx.room.Entity

@Entity(
    tableName = "routine_exercise",
    // [Req A] Tabla puente N:M entre rutinas y ejercicios.
    primaryKeys = ["routineId", "exerciseId"]
)
data class RoutineExerciseEntity(
    val routineId: Int,
    val exerciseId: String,
    // Progreso por ejercicio dentro de rutina.
    val isCompleted: Boolean = false,
    // Estado de sincronizacion pendiente para backend.
    val syncState: String = "PENDING_UPSERT",
    val deleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

