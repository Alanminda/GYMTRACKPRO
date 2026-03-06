package com.example.gymtrackpro.viewmodel

import androidx.lifecycle.*
import com.example.gymtrackpro.data.repository.GymRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: GymRepository) : ViewModel() {

    // Ahora observamos la base de datos local (Room) en lugar de pedir directamente a la API
    val exercises = repository.observeExercisesLocal().asLiveData()
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadExercises() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // El método correcto es refreshExercisesFromApi()
                repository.refreshExercisesFromApi()
            } catch (e: Exception) {
                // Manejar error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
