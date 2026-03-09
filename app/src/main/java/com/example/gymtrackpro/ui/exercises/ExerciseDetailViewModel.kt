/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/ui/exercises/ExerciseDetailViewModel.kt
 * Proposito: Exploracion y detalle de ejercicios, incluyendo agregar a rutina.
 */
package com.example.gymtrackpro.ui.exercises

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackpro.data.local.entities.ExerciseEntity
import com.example.gymtrackpro.data.repository.GymRepository
import kotlinx.coroutines.launch

class ExerciseDetailViewModel(private val repo: GymRepository) : ViewModel() {

    // [Req C - MVVM] Estado de detalle y mensajes de accion.
    private val _detail = MutableLiveData<ExerciseEntity?>(null)
    val detail: LiveData<ExerciseEntity?> = _detail

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    fun loadDetail(exerciseId: String) {
        // [Req B] Enriquecimiento de detalle desde API (si hay red) + cache local.
        viewModelScope.launch {
            try {
                _detail.value = repo.fetchExerciseDetailFromApi(exerciseId)
            } catch (_: Exception) {
                // Mantener data inicial si falla remoto.
            }
        }
    }

    fun addExerciseToRoutine(routineId: Int, exerciseId: String) {
        // [Req A] Create en tabla puente rutina-ejercicio.
        // [Req C] Si hay sesion, dispara sync remoto.
        viewModelScope.launch {
            try {
                repo.addExerciseToRoutine(routineId, exerciseId)
                if (repo.isLoggedIn()) {
                    repo.syncForLoggedUser()
                }
                _message.value = "Ejercicio anadido a la rutina"
            } catch (_: Exception) {
                _message.value = "No se pudo anadir el ejercicio"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

