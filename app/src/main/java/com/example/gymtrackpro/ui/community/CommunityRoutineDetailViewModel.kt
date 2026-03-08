package com.example.gymtrackpro.ui.community

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackpro.data.local.entities.ExerciseEntity
import com.example.gymtrackpro.data.repository.GymRepository
import kotlinx.coroutines.launch

class CommunityRoutineDetailViewModel(private val repo: GymRepository) : ViewModel() {

    private val _exercises = MutableLiveData<List<ExerciseEntity>>(emptyList())
    val exercises: LiveData<List<ExerciseEntity>> = _exercises

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun load(exerciseIds: List<String>) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _exercises.value = repo.getExercisesByIds(exerciseIds)
            } catch (_: Exception) {
                _error.value = "No se pudieron cargar los ejercicios"
            } finally {
                _loading.value = false
            }
        }
    }
}
