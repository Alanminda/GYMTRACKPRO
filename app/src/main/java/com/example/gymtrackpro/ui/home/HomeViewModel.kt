package com.example.gymtrackpro.ui.home

import androidx.lifecycle.*
import com.example.gymtrackpro.data.local.entities.ExerciseEntity
import com.example.gymtrackpro.data.repository.GymRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: GymRepository) : ViewModel() {

    val exercises = repo.observeExercisesLocal().asLiveData()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun refreshExercises() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repo.refreshExercisesFromApi()
            } catch (e: Exception) {
                _error.value = "Sin conexión o error API"
            } finally {
                _loading.value = false
            }
        }
    }

    fun syncProgress() {
        viewModelScope.launch { repo.syncAllPending() }
    }
}
