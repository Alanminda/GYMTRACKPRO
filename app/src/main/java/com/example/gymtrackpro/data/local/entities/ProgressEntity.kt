/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/data/local/entities/ProgressEntity.kt
 * Proposito: Define entidad local de Room para persistencia offline.
 */
package com.example.gymtrackpro.data.local.entities

import androidx.room.*

@Entity(
    tableName = "progress",
    foreignKeys = [
        // [Req A] Relacion 1:N -> un usuario tiene multiples registros de progreso.
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
    // Control local para envio diferido cuando no hay red.
    val synced: Boolean = false
)

