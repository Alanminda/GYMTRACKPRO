package com.example.gymtrackpro.data.local.entities

import androidx.room.*

@Entity(
    tableName = "routines",
    foreignKeys = [
        ForeignKey(
            entity = UserLocalEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val userId: Int
)
