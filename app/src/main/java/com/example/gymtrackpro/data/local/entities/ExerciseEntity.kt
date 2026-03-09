/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/data/local/entities/ExerciseEntity.kt
 * Proposito: Define entidad local de Room para persistencia offline.
 */
package com.example.gymtrackpro.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    // [Req A/B] Cache local del catalogo externo consumido por Retrofit.
    @PrimaryKey val id: String, // conserva id remoto estable para sync y detalle.
    val name: String,
    val muscleGroup: String,
    val gifUrl: String? = null,
    val bodyPart: String? = null,
    val equipment: String? = null,
    val target: String? = null,
    val secondaryMuscles: String? = null,
    val instructions: String? = null
)

