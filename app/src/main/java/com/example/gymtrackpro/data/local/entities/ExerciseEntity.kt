package com.example.gymtrackpro.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey val id: String, // para poder guardar el _id de Mongo
    val name: String,
    val muscleGroup: String,
    val bodyPart: String? = null,
    val equipment: String? = null,
    val target: String? = null
)
