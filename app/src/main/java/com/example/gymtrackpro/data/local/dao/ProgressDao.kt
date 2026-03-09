/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/data/local/dao/ProgressDao.kt
 * Proposito: Define operaciones de acceso a datos (DAO) para Room.
 */
package com.example.gymtrackpro.data.local.dao

import androidx.room.*
import com.example.gymtrackpro.data.local.entities.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    // [Req A - Read] Observa historial de progreso del usuario.
    @Query("SELECT * FROM progress WHERE userId = :userId ORDER BY id DESC")
    fun observeByUser(userId: Int): Flow<List<ProgressEntity>>

    // [Req A - Create] Inserta nuevo registro de progreso.
    @Insert
    suspend fun insert(item: ProgressEntity)

    // [Req A - Read] Recupera pendientes para sincronizacion remota.
    @Query("SELECT * FROM progress WHERE synced = 0")
    suspend fun getPendingSync(): List<ProgressEntity>

    // [Req A - Update] Marca registro como sincronizado.
    @Query("UPDATE progress SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)
}

