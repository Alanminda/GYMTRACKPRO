package com.example.gymtrackpro.data.repository

import com.example.gymtrackpro.data.local.dao.ExerciseDao
import com.example.gymtrackpro.data.local.dao.ProgressDao
import com.example.gymtrackpro.data.local.dao.RoutineDao
import com.example.gymtrackpro.data.local.dao.RoutineExerciseDao
import com.example.gymtrackpro.data.local.dao.UserLocalDao
import com.example.gymtrackpro.data.local.entities.ExerciseEntity
import com.example.gymtrackpro.data.local.entities.ProgressEntity
import com.example.gymtrackpro.data.local.entities.RoutineEntity
import com.example.gymtrackpro.data.local.entities.RoutineExerciseEntity
import com.example.gymtrackpro.data.local.entities.UserLocalEntity
import com.example.gymtrackpro.data.remote.api.ApiService
import com.example.gymtrackpro.data.remote.dto.*
import kotlinx.coroutines.flow.Flow

class GymRepository(
    private val userDao: UserLocalDao,
    private val exerciseDao: ExerciseDao,
    private val progressDao: ProgressDao,
    private val routineDao: RoutineDao,
    private val routineExerciseDao: RoutineExerciseDao,
    private val api: ApiService
) {

    suspend fun getSession(): UserLocalEntity? = userDao.getSession()

    suspend fun register(name: String, email: String, password: String) {
        val res = api.register(RegisterRequest(name, email, password))
        userDao.saveSession(UserLocalEntity(id = 1, name = res.name, email = res.email, token = res.token))
    }

    suspend fun login(email: String, password: String) {
        val res = api.login(LoginRequest(email, password))
        userDao.saveSession(UserLocalEntity(id = 1, name = res.name, email = res.email, token = res.token))
    }

    suspend fun logout() = userDao.clearSession()

    fun observeExercisesLocal(): Flow<List<ExerciseEntity>> = exerciseDao.observeAll()

    suspend fun refreshExercisesFromApi() {
        val session = userDao.getSession() ?: throw IllegalStateException("No hay sesion")
        val remote = api.getExercises("Bearer ${session.token}")
        val mapped = remote.mapNotNull { dto ->
            val resolvedId = dto.mongoId ?: dto.id
            resolvedId?.let {
                ExerciseEntity(
                    id = it,
                    name = dto.name,
                    muscleGroup = dto.muscleGroup ?: dto.target ?: "general",
                    bodyPart = dto.bodyPart,
                    equipment = dto.equipment,
                    target = dto.target
                )
            }
        }
        exerciseDao.upsertAll(mapped)
    }

    fun observeProgress(userId: Int): Flow<List<ProgressEntity>> = progressDao.observeByUser(userId)

    suspend fun addProgress(dateIso: String, weight: Double, note: String?) {
        progressDao.insert(
            ProgressEntity(dateIso = dateIso, weight = weight, note = note, userId = 1, synced = false)
        )
    }

    suspend fun syncPendingProgress() {
        val session = userDao.getSession() ?: return
        val pending = progressDao.getPendingSync()
        for (p in pending) {
            val resp = api.sendProgress(
                bearer = "Bearer ${session.token}",
                body = ProgressUpsertRequest(p.dateIso, p.weight, p.note)
            )
            if (resp.isSuccessful) progressDao.markSynced(p.id)
        }
    }

    fun observeRoutinesLocal(userId: Int = 1): Flow<List<RoutineEntity>> = routineDao.observeActiveByUser(userId)

    suspend fun createRoutine(name: String, userId: Int = 1): Int {
        val routine = RoutineEntity(name = name, userId = userId)
        return routineDao.insert(routine).toInt()
    }

    suspend fun renameRoutine(routineId: Int, newName: String) {
        val current = routineDao.getById(routineId) ?: return
        routineDao.update(
            current.copy(
                name = newName,
                syncState = "PENDING_UPSERT",
                deleted = false,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteRoutine(routineId: Int) {
        val current = routineDao.getById(routineId) ?: return
        if (current.remoteId.isNullOrBlank()) {
            routineExerciseDao.hardDeleteByRoutine(routineId)
            routineDao.hardDelete(routineId)
            return
        }
        routineDao.markPendingDelete(routineId)
    }

    suspend fun addExerciseToRoutine(routineId: Int, exerciseId: String) {
        routineExerciseDao.upsert(
            RoutineExerciseEntity(
                routineId = routineId,
                exerciseId = exerciseId,
                syncState = "PENDING_UPSERT",
                deleted = false,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeExerciseFromRoutine(routineId: Int, exerciseId: String) {
        routineExerciseDao.markPendingDelete(
            routineId = routineId,
            exerciseId = exerciseId,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun syncPendingRoutines() {
        val session = userDao.getSession() ?: return
        val bearer = "Bearer ${session.token}"
        val pendingRoutines = routineDao.getPendingSync()

        for (routine in pendingRoutines) {
            try {
                if (routine.deleted) {
                    if (!routine.remoteId.isNullOrBlank()) {
                        val deleteResp = api.deleteRoutine(bearer = bearer, id = routine.remoteId)
                        if (!deleteResp.isSuccessful) continue
                    }
                    routineExerciseDao.hardDeleteByRoutine(routine.id)
                    routineDao.hardDelete(routine.id)
                    continue
                }

                val remote = if (routine.remoteId.isNullOrBlank()) {
                    api.createRoutine(
                        bearer = bearer,
                        body = RoutineUpsertRequest(name = routine.name)
                    )
                } else {
                    api.updateRoutine(
                        bearer = bearer,
                        id = routine.remoteId,
                        body = RoutineUpsertRequest(name = routine.name)
                    )
                }

                routineDao.markSynced(localId = routine.id, remoteId = remote._id)
                syncRoutineExercisesBySet(routineId = routine.id, remoteRoutineId = remote._id, bearer = bearer)
            } catch (_: Exception) {
                // Queda pendiente para el proximo intento.
            }
        }
    }

    suspend fun syncAllPending() {
        syncPendingRoutines()
        syncPendingProgress()
    }

    private suspend fun syncRoutineExercisesBySet(routineId: Int, remoteRoutineId: String, bearer: String) {
        val exerciseIds = routineExerciseDao.getActiveExerciseIds(routineId)
        val resp = api.syncRoutineExercises(
            bearer = bearer,
            id = remoteRoutineId,
            body = RoutineExerciseSyncRequest(exerciseIds = exerciseIds)
        )
        if (resp.isSuccessful) {
            routineExerciseDao.markRoutineSynced(routineId)
        }
    }
}
