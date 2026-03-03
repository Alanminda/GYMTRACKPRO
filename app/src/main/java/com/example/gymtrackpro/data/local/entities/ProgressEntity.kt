package com.example.gymtrackpro.data.local.entities

import androidx.room.*

@Entity(
    tableName = "progress",
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
data class ProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateIso: String,
    val weight: Double,
    val note: String?,
    val userId: Int,
    val synced: Boolean = false
)
