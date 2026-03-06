package com.example.gymtrackpro.data.local.dao

import androidx.room.*
import com.example.gymtrackpro.data.local.entities.RoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Query("SELECT * FROM routines WHERE userId = :userId AND deleted = 0 ORDER BY updatedAt DESC")
    fun observeActiveByUser(userId: Int): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): RoutineEntity?

    @Query("SELECT * FROM routines WHERE syncState != 'SYNCED' OR deleted = 1")
    suspend fun getPendingSync(): List<RoutineEntity>

    @Insert
    suspend fun insert(item: RoutineEntity): Long

    @Update
    suspend fun update(item: RoutineEntity)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun hardDelete(id: Int)

    @Query("UPDATE routines SET remoteId = :remoteId, syncState = 'SYNCED', deleted = 0, updatedAt = :updatedAt WHERE id = :localId")
    suspend fun markSynced(localId: Int, remoteId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE routines SET syncState = 'PENDING_DELETE', deleted = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markPendingDelete(id: Int, updatedAt: Long = System.currentTimeMillis())
}
