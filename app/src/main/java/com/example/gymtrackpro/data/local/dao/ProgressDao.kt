package com.example.gymtrackpro.data.local.dao

import androidx.room.*
import com.example.gymtrackpro.data.local.entities.ProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress WHERE userId = :userId ORDER BY id DESC")
    fun observeByUser(userId: Int): Flow<List<ProgressEntity>>

    @Insert
    suspend fun insert(item: ProgressEntity)

    @Query("SELECT * FROM progress WHERE synced = 0")
    suspend fun getPendingSync(): List<ProgressEntity>

    @Query("UPDATE progress SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)
}
