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
    indices = [Index("userId"), Index(value = ["remoteId"], unique = true)]
)
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val remoteId: String? = null,
    val name: String,
    val userId: Int,
    val syncState: String = "PENDING_UPSERT",
    val deleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
