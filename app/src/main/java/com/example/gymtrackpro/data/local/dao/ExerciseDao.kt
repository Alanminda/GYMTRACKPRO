package com.example.gymtrackpro.data.local.dao

import androidx.room.*
import com.example.gymtrackpro.data.local.entities.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ExerciseEntity>)

    @Query("DELETE FROM exercises")
    suspend fun clear()
}
