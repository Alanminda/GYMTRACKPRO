/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/data/local/entities/RoutineEntity.kt
 * Proposito: Define entidad local de Room para persistencia offline.
 */
package com.example.gymtrackpro.data.local.entities

import androidx.room.*

@Entity(
    tableName = "routines",
    foreignKeys = [
        // [Req A] Relacion 1:N -> un usuario local puede tener muchas rutinas.
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
    // Id remoto para sincronizacion con backend (local vs remota).
    val remoteId: String? = null,
    // Id de rutina publica (comunidad/favoritos).
    val publicRoutineId: String? = null,
    val routineType: String = "OWN",
    val ownerName: String? = null,
    val name: String,
    // Estado local de completitud (UX).
    val isCompleted: Boolean = false,
    val userId: Int,
    // [Req C] Sync state para estrategia offline-first en Repository.
    val syncState: String = "PENDING_UPSERT",
    val deleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

