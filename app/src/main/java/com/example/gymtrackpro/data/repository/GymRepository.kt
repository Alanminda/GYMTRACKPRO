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
    suspend fun isLoggedIn(): Boolean = userDao.getSession() != null

    suspend fun register(name: String, email: String, password: String) {
        val res = api.register(RegisterRequest(name, email, password))
        userDao.saveSession(UserLocalEntity(id = 1, name = res.name, email = res.email, token = res.token))
    }

    suspend fun login(email: String, password: String) {
        val res = api.login(LoginRequest(email, password))
        userDao.saveSession(UserLocalEntity(id = 1, name = res.name, email = res.email, token = res.token))
    }

    suspend fun logout() = userDao.clearSession()

    suspend fun fetchProfile(): ProfileDto {
        val session = userDao.getSession() ?: throw IllegalStateException("No hay sesion")
        return api.getProfile("Bearer ${session.token}")
    }

    suspend fun updateProfile(name: String, email: String): ProfileDto {
        val session = userDao.getSession() ?: throw IllegalStateException("No hay sesion")
        val updated = api.updateProfile(
            bearer = "Bearer ${session.token}",
            body = ProfileUpdateRequest(name = name.trim(), email = email.trim())
        )
        userDao.saveSession(
            session.copy(
                name = updated.name,
                email = updated.email
            )
        )
        return updated
    }

    fun observeExercisesLocal(): Flow<List<ExerciseEntity>> = exerciseDao.observeAll()

    suspend fun refreshExercisesFromApi() {
        val firstPage = fetchExercisesPageFromApi(query = null, limit = 200, offset = 0)
        if (firstPage.isNotEmpty()) {
            exerciseDao.upsertAll(firstPage)
        }
    }

    suspend fun fetchExercisesPageFromApi(query: String?, limit: Int, offset: Int): List<ExerciseEntity> {
        val session = userDao.getSession() ?: throw IllegalStateException("No hay sesion")
        val remote = api.getExercises(
            bearer = "Bearer ${session.token}",
            limit = limit,
            offset = offset,
            q = query?.takeIf { it.isNotBlank() }
        )

        val mapped = remote.mapNotNull { dto ->
            val resolvedId = dto.mongoId ?: dto.id
            resolvedId?.let {
                ExerciseEntity(
                    id = it,
                    name = dto.name,
                    muscleGroup = dto.muscleGroup ?: dto.target ?: "general",
                    gifUrl = dto.gifUrl,
                    bodyPart = dto.bodyPart,
                    equipment = dto.equipment,
                    target = dto.target,
                    secondaryMuscles = dto.secondaryMuscles?.joinToString(", "),
                    instructions = dto.instructions?.joinToString("\n")
                )
            }
        }

        if (mapped.isNotEmpty()) {
            exerciseDao.upsertAll(mapped)
        }
        return mapped
    }

    suspend fun fetchExerciseDetailFromApi(exerciseId: String): ExerciseEntity? {
        val session = userDao.getSession() ?: return null
        val dto = api.getExerciseById(
            bearer = "Bearer ${session.token}",
            id = exerciseId
        )
        val resolvedId = dto.mongoId ?: dto.id ?: exerciseId
        val mapped = ExerciseEntity(
            id = resolvedId,
            name = dto.name,
            muscleGroup = dto.muscleGroup ?: dto.target ?: "general",
            gifUrl = dto.gifUrl,
            bodyPart = dto.bodyPart,
            equipment = dto.equipment,
            target = dto.target,
            secondaryMuscles = dto.secondaryMuscles?.joinToString(", "),
            instructions = dto.instructions?.joinToString("\n")
        )
        exerciseDao.upsertAll(listOf(mapped))
        return mapped
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
    fun observeFavoriteRoutinesLocal(userId: Int = 1): Flow<List<RoutineEntity>> = routineDao.observeByType(userId, "FAVORITE")
    suspend fun getRoutinesLocal(userId: Int = 1): List<RoutineEntity> = routineDao.getActiveByUser(userId)
    suspend fun hasPendingRoutineSync(): Boolean = routineDao.getPendingSync().isNotEmpty()

    suspend fun getRoutineExercises(routineId: Int): List<ExerciseEntity> {
        val exerciseIds = routineExerciseDao.getActiveExerciseIds(routineId)
        if (exerciseIds.isEmpty()) return emptyList()
        return resolveExercisesByIds(exerciseIds)
    }

    suspend fun getExercisesByIds(exerciseIds: List<String>): List<ExerciseEntity> {
        if (exerciseIds.isEmpty()) return emptyList()
        return resolveExercisesByIds(exerciseIds.distinct())
    }

    suspend fun createRoutine(name: String, userId: Int = 1): Int {
        val routine = RoutineEntity(name = name, userId = userId, routineType = "OWN")
        return routineDao.insert(routine).toInt()
    }

    suspend fun renameRoutine(routineId: Int, newName: String) {
        val current = routineDao.getById(routineId) ?: return
        if (current.routineType != "OWN") return
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
        if (current.routineType == "FAVORITE") {
            if (isLoggedIn() && !current.publicRoutineId.isNullOrBlank()) {
                val session = userDao.getSession()
                if (session != null) {
                    api.unfavoriteCommunityRoutine(
                        bearer = "Bearer ${session.token}",
                        id = current.publicRoutineId
                    )
                }
            }
            routineExerciseDao.hardDeleteByRoutine(routineId)
            routineDao.hardDelete(routineId)
            return
        }
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
        val routine = routineDao.getById(routineId)
        if (routine != null && !routine.deleted && routine.routineType == "OWN") {
            routineDao.markPendingUpsert(routineId)
        }
    }

    suspend fun removeExerciseFromRoutine(routineId: Int, exerciseId: String) {
        routineExerciseDao.markPendingDelete(
            routineId = routineId,
            exerciseId = exerciseId,
            updatedAt = System.currentTimeMillis()
        )
        val routine = routineDao.getById(routineId)
        if (routine != null && !routine.deleted && routine.routineType == "OWN") {
            routineDao.markPendingUpsert(routineId)
        }
    }

    suspend fun syncPendingRoutines() {
        val session = userDao.getSession() ?: return
        val bearer = "Bearer ${session.token}"
        ensureRoutinePendingFromRoutineExerciseChanges()
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

    suspend fun syncForLoggedUser() {
        if (!isLoggedIn()) return
        syncAllPending()
        pullRemoteRoutinesToLocal()
        pullFavoriteCommunityRoutinesToLocal()
    }

    private suspend fun pullRemoteRoutinesToLocal() {
        val session = userDao.getSession() ?: return
        val bearer = "Bearer ${session.token}"
        val remoteRoutines = api.getRoutines(bearer)

        val allExerciseIds = linkedSetOf<String>()
        for (remote in remoteRoutines) {
            val existing = routineDao.getByRemoteId(remote._id)
            val localId = if (existing == null) {
                routineDao.insert(
                    RoutineEntity(
                        remoteId = remote._id,
                        name = remote.name,
                        routineType = "OWN",
                        userId = 1,
                        syncState = "SYNCED",
                        deleted = false,
                        updatedAt = System.currentTimeMillis()
                    )
                ).toInt()
            } else {
                routineDao.update(
                    existing.copy(
                        name = remote.name,
                        routineType = "OWN",
                        syncState = "SYNCED",
                        deleted = false,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                existing.id
            }

            routineExerciseDao.hardDeleteByRoutine(localId)
            remote.exerciseIds.distinct().forEach { exerciseId ->
                allExerciseIds.add(exerciseId)
                routineExerciseDao.upsert(
                    RoutineExerciseEntity(
                        routineId = localId,
                        exerciseId = exerciseId,
                        syncState = "SYNCED",
                        deleted = false,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        if (allExerciseIds.isNotEmpty()) {
            val localExerciseIds = exerciseDao.getByIds(allExerciseIds.toList()).map { it.id }.toSet()
            val missing = allExerciseIds.filter { it !in localExerciseIds }
            for (exerciseId in missing) {
                try {
                    fetchExerciseDetailFromApi(exerciseId)
                } catch (_: Exception) {
                    // No bloquear sync por un ejercicio puntual.
                }
            }
        }
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

    suspend fun shareRoutine(routineId: Int): CommunityRoutineDto {
        val session = userDao.getSession() ?: throw IllegalStateException("No hay sesion")
        val routine = routineDao.getById(routineId) ?: throw IllegalStateException("Rutina no encontrada")
        if (routine.routineType != "OWN") throw IllegalStateException("Solo se comparten rutinas propias")
        val remoteId = routine.remoteId ?: throw IllegalStateException("Rutina aun no sincronizada")

        return api.shareRoutine(
            bearer = "Bearer ${session.token}",
            id = remoteId
        )
    }

    suspend fun fetchCommunityRoutinesPage(
        limit: Int,
        offset: Int,
        query: String?,
        sort: String
    ): List<CommunityRoutineDto> {
        val session = userDao.getSession() ?: throw IllegalStateException("No hay sesion")
        return api.getCommunityRoutines(
            bearer = "Bearer ${session.token}",
            limit = limit,
            offset = offset,
            q = query?.takeIf { it.isNotBlank() },
            sort = sort
        )
    }

    suspend fun setCommunityRoutineFavorite(publicRoutineId: String, favorite: Boolean): CommunityFavoriteToggleDto {
        val session = userDao.getSession() ?: throw IllegalStateException("No hay sesion")
        val bearer = "Bearer ${session.token}"
        val resp = if (favorite) {
            api.favoriteCommunityRoutine(bearer = bearer, id = publicRoutineId)
        } else {
            api.unfavoriteCommunityRoutine(bearer = bearer, id = publicRoutineId)
        }
        if (!resp.isSuccessful) throw IllegalStateException("No se pudo actualizar favorito")
        val payload = resp.body() ?: CommunityFavoriteToggleDto(
            message = if (favorite) "Favorito agregado" else "Favorito quitado",
            favoritesCount = 0,
            isFavorite = favorite
        )
        pullFavoriteCommunityRoutinesToLocal()
        return payload
    }

    private suspend fun pullFavoriteCommunityRoutinesToLocal() {
        val session = userDao.getSession() ?: return
        val favoriteRemote = api.getCommunityFavorites("Bearer ${session.token}")

        val localFavorite = routineDao.getFavoritesByUser(userId = 1)
        val localByPublicId = localFavorite.associateBy { it.publicRoutineId }
        val incomingIds = favoriteRemote.map { it.id }.toSet()

        for (local in localFavorite) {
            if (local.publicRoutineId !in incomingIds) {
                routineExerciseDao.hardDeleteByRoutine(local.id)
                routineDao.hardDelete(local.id)
            }
        }

        val allExerciseIds = linkedSetOf<String>()
        for (remote in favoriteRemote) {
            val existing = localByPublicId[remote.id]
            val localId = if (existing == null) {
                val normalizedName = withDurationInName(remote.name, remote.durationMinutes)
                routineDao.insert(
                    RoutineEntity(
                        publicRoutineId = remote.id,
                        routineType = "FAVORITE",
                        ownerName = remote.ownerName,
                        name = normalizedName,
                        userId = 1,
                        syncState = "SYNCED",
                        deleted = false,
                        updatedAt = System.currentTimeMillis()
                    )
                ).toInt()
            } else {
                val normalizedName = withDurationInName(remote.name, remote.durationMinutes)
                routineDao.update(
                    existing.copy(
                        name = normalizedName,
                        ownerName = remote.ownerName,
                        routineType = "FAVORITE",
                        syncState = "SYNCED",
                        deleted = false,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                existing.id
            }

            routineExerciseDao.hardDeleteByRoutine(localId)
            remote.exerciseIds.distinct().forEach { exerciseId ->
                allExerciseIds.add(exerciseId)
                routineExerciseDao.upsert(
                    RoutineExerciseEntity(
                        routineId = localId,
                        exerciseId = exerciseId,
                        syncState = "SYNCED",
                        deleted = false,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        if (allExerciseIds.isNotEmpty()) {
            val localExerciseIds = exerciseDao.getByIds(allExerciseIds.toList()).map { it.id }.toSet()
            val missing = allExerciseIds.filter { it !in localExerciseIds }
            for (exerciseId in missing) {
                try {
                    fetchExerciseDetailFromApi(exerciseId)
                } catch (_: Exception) {
                    // No bloquear sync por un ejercicio puntual.
                }
            }
        }
    }

    private suspend fun ensureRoutinePendingFromRoutineExerciseChanges() {
        val pendingRoutineIds = routineExerciseDao.getPendingSync()
            .map { it.routineId }
            .distinct()

        for (routineId in pendingRoutineIds) {
            val routine = routineDao.getById(routineId) ?: continue
            if (!routine.deleted && routine.routineType == "OWN") {
                routineDao.markPendingUpsert(routineId)
            }
        }
    }

    private suspend fun resolveExercisesByIds(exerciseIds: List<String>): List<ExerciseEntity> {
        val local = exerciseDao.getByIds(exerciseIds)
        val localById = local.associateBy { it.id }
        val missingIds = exerciseIds.filter { !localById.containsKey(it) }

        if (missingIds.isNotEmpty() && isLoggedIn()) {
            for (id in missingIds) {
                try {
                    fetchExerciseDetailFromApi(id)
                } catch (_: Exception) {
                    // Si falla un detalle puntual, continuar con los demas.
                }
            }
        }

        val refreshed = exerciseDao.getByIds(exerciseIds).associateBy { it.id }
        return exerciseIds.mapNotNull { refreshed[it] }
    }

    private fun withDurationInName(name: String, durationMinutes: Int?): String {
        if (durationMinutes == null || durationMinutes <= 0) return name
        if (Regex("\\(\\d+\\s*min\\)", RegexOption.IGNORE_CASE).containsMatchIn(name)) return name
        return "$name (${durationMinutes} min)"
    }
}
