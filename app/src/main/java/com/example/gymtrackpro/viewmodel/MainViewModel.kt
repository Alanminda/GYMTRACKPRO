/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/viewmodel/MainViewModel.kt
 * Proposito: ViewModel legado/de apoyo para pantallas base.
 */
package com.example.gymtrackpro.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.gymtrackpro.data.repository.GymRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: GymRepository) : ViewModel() {

    // [Req A] Lee ejercicios desde Room.
    // [Req C - MVVM] La UI observa este LiveData, no accede al repositorio directamente.
    val exercises = repository.observeExercisesLocal().asLiveData()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadExercises() {
        // [Req C - Corrutinas] Trabajo de red en segundo plano.
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // [Req B] Consume API y refresca cache local.
                repository.refreshExercisesFromApi()
            } catch (_: Exception) {
                // Manejo de error simplificado en este ViewModel legacy.
            } finally {
                _isLoading.value = false
            }
        }
    }
}
