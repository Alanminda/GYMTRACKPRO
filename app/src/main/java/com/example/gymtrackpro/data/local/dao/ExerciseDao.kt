/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/data/local/dao/ExerciseDao.kt
 * Proposito: Define operaciones de acceso a datos (DAO) para Room.
 */
package com.example.gymtrackpro.data.local.dao

import androidx.room.*
import com.example.gymtrackpro.data.local.entities.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    // [Req A - Read] Stream reactivo para UI (RecyclerView) desde Room.
    @Query("SELECT * FROM exercises")
    fun observeAll(): Flow<List<ExerciseEntity>>

    // [Req A - Read] Consulta por ids para construir detalle de rutina/comunidad.
    @Query("SELECT * FROM exercises WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<ExerciseEntity>

    @Query(
        """
        SELECT *
        FROM exercises
        WHERE (
          :query = ''
          OR lower(name) LIKE '%' || lower(:query) || '%'
          OR lower(muscleGroup) LIKE '%' || lower(:query) || '%'
          OR lower(COALESCE(target, '')) LIKE '%' || lower(:query) || '%'
          OR lower(COALESCE(bodyPart, '')) LIKE '%' || lower(:query) || '%'
          OR lower(COALESCE(equipment, '')) LIKE '%' || lower(:query) || '%'
        )
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getAllPaged(query: String, limit: Int, offset: Int): List<ExerciseEntity>

    @Query(
        """
        SELECT DISTINCT e.*
        FROM exercises e
        INNER JOIN routine_exercise re ON re.exerciseId = e.id
        WHERE re.deleted = 0
          AND (
            :query = ''
            OR lower(e.name) LIKE '%' || lower(:query) || '%'
            OR lower(e.muscleGroup) LIKE '%' || lower(:query) || '%'
            OR lower(COALESCE(e.target, '')) LIKE '%' || lower(:query) || '%'
          )
        ORDER BY e.name ASC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getUsedInRoutinesPaged(query: String, limit: Int, offset: Int): List<ExerciseEntity>

    // [Req A - Create/Update] UPSERT del catalogo remoto cacheado localmente.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ExerciseEntity>)

    // [Req A - Delete] Limpieza total de cache.
    @Query("DELETE FROM exercises")
    suspend fun clear()
}

