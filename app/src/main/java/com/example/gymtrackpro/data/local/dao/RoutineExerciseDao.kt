package com.example.gymtrackpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gymtrackpro.data.local.entities.RoutineExerciseEntity

@Dao
interface RoutineExerciseDao {

    @Query("SELECT * FROM routine_exercise WHERE routineId = :routineId AND deleted = 0")
    suspend fun getActiveByRoutine(routineId: Int): List<RoutineExerciseEntity>

    @Query("SELECT exerciseId FROM routine_exercise WHERE routineId = :routineId AND deleted = 0")
    suspend fun getActiveExerciseIds(routineId: Int): List<String>

    @Query("SELECT exerciseId FROM routine_exercise WHERE routineId = :routineId AND deleted = 0 AND isCompleted = 1")
    suspend fun getCompletedExerciseIds(routineId: Int): List<String>

    @Query("SELECT * FROM routine_exercise WHERE syncState != 'SYNCED' OR deleted = 1")
    suspend fun getPendingSync(): List<RoutineExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: RoutineExerciseEntity)

    @Query("UPDATE routine_exercise SET syncState = 'PENDING_DELETE', deleted = 1, updatedAt = :updatedAt WHERE routineId = :routineId AND exerciseId = :exerciseId")
    suspend fun markPendingDelete(routineId: Int, exerciseId: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE routine_exercise SET syncState = 'SYNCED', deleted = 0, updatedAt = :updatedAt WHERE routineId = :routineId")
    suspend fun markRoutineSynced(routineId: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE routine_exercise SET isCompleted = :completed, updatedAt = :updatedAt WHERE routineId = :routineId AND exerciseId = :exerciseId")
    suspend fun setCompleted(
        routineId: Int,
        exerciseId: String,
        completed: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE routine_exercise SET isCompleted = 0, updatedAt = :updatedAt WHERE routineId = :routineId AND deleted = 0 AND isCompleted = 1")
    suspend fun clearCompletedByRoutine(
        routineId: Int,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM routine_exercise WHERE routineId = :routineId")
    suspend fun hardDeleteByRoutine(routineId: Int)
}
