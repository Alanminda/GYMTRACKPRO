/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/ui/routines/RoutineDetailViewModel.kt
 * Proposito: Detalle/seleccion de rutinas y acciones sobre ejercicios internos.
 */
package com.example.gymtrackpro.ui.routines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackpro.data.local.entities.ExerciseEntity
import com.example.gymtrackpro.data.repository.GymRepository
import kotlinx.coroutines.launch

class RoutineDetailViewModel(private val repo: GymRepository) : ViewModel() {

    // [Req C - MVVM] Estado de detalle y progreso por ejercicio.
    private val _exercises = MutableLiveData<List<ExerciseEntity>>(emptyList())
    val exercises: LiveData<List<ExerciseEntity>> = _exercises
    private val _completedExerciseIds = MutableLiveData<Set<String>>(emptySet())
    val completedExerciseIds: LiveData<Set<String>> = _completedExerciseIds

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error
    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    private var currentRoutineId: Int = -1

    fun loadRoutineExercises(routineId: Int) {
        // [Req C - MVVM] La View solicita datos, el ViewModel orquesta via Repository.
        currentRoutineId = routineId
        viewModelScope.launch {
            // [Req A - Read] Lee ejercicios de rutina desde Room (con enriquecimiento remoto si falta detalle).
            _loading.value = true
            _error.value = null
            try {
                _exercises.value = repo.getRoutineExercises(routineId)
                _completedExerciseIds.value = repo.getRoutineCompletedExerciseIds(routineId)
            } catch (_: Exception) {
                _error.value = "No se pudieron cargar los ejercicios de la rutina"
            } finally {
                _loading.value = false
            }
        }
    }

    fun toggleExerciseCompleted(exerciseId: String) {
        // [Req A - Update] Operacion CRUD de estado por ejercicio.
        val routineId = currentRoutineId
        if (routineId <= 0) return
        viewModelScope.launch {
            // [Req A - Update] Marca/desmarca ejercicio como hecho.
            try {
                val completed = repo.toggleRoutineExerciseCompleted(routineId, exerciseId)
                _completedExerciseIds.value = repo.getRoutineCompletedExerciseIds(routineId)
                _message.value = if (completed) "Ejercicio marcado como hecho" else "Ejercicio desmarcado"
            } catch (_: Exception) {
                _message.value = "No se pudo actualizar el ejercicio"
            }
        }
    }

    fun removeExercise(exerciseId: String) {
        // [Req A - Delete] Quita ejercicio de la rutina local (y sync pendiente si aplica).
        val routineId = currentRoutineId
        if (routineId <= 0) return
        viewModelScope.launch {
            // [Req A - Delete] Elimina ejercicio de rutina propia.
            try {
                repo.removeExerciseFromRoutine(routineId, exerciseId)
                _exercises.value = repo.getRoutineExercises(routineId)
                _completedExerciseIds.value = repo.getRoutineCompletedExerciseIds(routineId)
                _message.value = "Ejercicio eliminado de la rutina"
            } catch (_: Exception) {
                _message.value = "No se pudo eliminar el ejercicio"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun clearAllCompletedExercises() {
        // [Req A - Update] Accion masiva para reiniciar avance de rutina.
        val routineId = currentRoutineId
        if (routineId <= 0) return
        viewModelScope.launch {
            // [Req A - Update] Limpieza masiva de estado completado.
            try {
                repo.clearRoutineCompletedExercises(routineId)
                _completedExerciseIds.value = repo.getRoutineCompletedExerciseIds(routineId)
                _message.value = "Se desmarcaron todos los ejercicios"
            } catch (_: Exception) {
                _message.value = "No se pudo desmarcar ejercicios"
            }
        }
    }
}

