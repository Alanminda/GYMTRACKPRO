package com.example.gymtrackpro.ui.exercises

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackpro.data.local.entities.ExerciseEntity
import com.example.gymtrackpro.data.repository.GymRepository
import kotlinx.coroutines.launch

class ExerciseDetailViewModel(private val repo: GymRepository) : ViewModel() {

    private val _detail = MutableLiveData<ExerciseEntity?>(null)
    val detail: LiveData<ExerciseEntity?> = _detail

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    fun loadDetail(exerciseId: String) {
        viewModelScope.launch {
            try {
                _detail.value = repo.fetchExerciseDetailFromApi(exerciseId)
            } catch (_: Exception) {
                // Mantener data inicial si falla remoto.
            }
        }
    }

    fun addExerciseToRoutine(routineId: Int, exerciseId: String) {
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
