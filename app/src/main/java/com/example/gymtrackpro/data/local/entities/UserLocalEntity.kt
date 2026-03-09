/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/data/local/entities/UserLocalEntity.kt
 * Proposito: Define entidad local de Room para persistencia offline.
 */
package com.example.gymtrackpro.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_local")
data class UserLocalEntity(
    // [Req A] Sesion local (offline-first): permite operar sin pedir login en cada pantalla.
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val token: String
)

