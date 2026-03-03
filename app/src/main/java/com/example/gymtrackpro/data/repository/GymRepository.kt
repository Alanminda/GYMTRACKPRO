package com.example.gymtrackpro.data.repository

import com.example.gymtrackpro.data.local.dao.ExerciseDao
import com.example.gymtrackpro.data.local.dao.ProgressDao
import com.example.gymtrackpro.data.local.dao.UserLocalDao
import com.example.gymtrackpro.data.local.entities.ExerciseEntity
import com.example.gymtrackpro.data.local.entities.ProgressEntity
import com.example.gymtrackpro.data.local.entities.UserLocalEntity
import com.example.gymtrackpro.data.remote.api.ApiService
import com.example.gymtrackpro.data.remote.dto.*
import kotlinx.coroutines.flow.Flow

class GymRepository(
    private val userDao: UserLocalDao,
    private val exerciseDao: ExerciseDao,
    private val progressDao: ProgressDao,
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
        val session = userDao.getSession() ?: throw IllegalStateException("No hay sesión")
        val remote = api.getExercises("Bearer ${session.token}")
        val mapped = remote.map { ExerciseEntity(id = it._id, name = it.name, muscleGroup = it.muscleGroup) }
        exerciseDao.upsertAll(mapped)
    }

    fun observeProgress(userId: Int): Flow<List<ProgressEntity>> = progressDao.observeByUser(userId)

    suspend fun addProgress(dateIso: String, weight: Double, note: String?) {
        // se guarda offline primero
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
}
