package com.example.gymtrackpro.ui.routines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackpro.data.local.entities.ExerciseEntity
import com.example.gymtrackpro.data.repository.GymRepository
import kotlinx.coroutines.launch

class RoutineDetailViewModel(private val repo: GymRepository) : ViewModel() {

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
        currentRoutineId = routineId
        viewModelScope.launch {
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
        val routineId = currentRoutineId
        if (routineId <= 0) return
        viewModelScope.launch {
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
        val routineId = currentRoutineId
        if (routineId <= 0) return
        viewModelScope.launch {
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
        val routineId = currentRoutineId
        if (routineId <= 0) return
        viewModelScope.launch {
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
