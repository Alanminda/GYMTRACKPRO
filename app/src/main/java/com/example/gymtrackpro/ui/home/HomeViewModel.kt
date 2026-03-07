package com.example.gymtrackpro.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.gymtrackpro.data.repository.GymRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: GymRepository) : ViewModel() {

    val routines = repo.observeRoutinesLocal().asLiveData()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _routineMessage = MutableLiveData<String?>(null)
    val routineMessage: LiveData<String?> = _routineMessage

    fun syncData() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repo.syncAllPending()
            } catch (e: Exception) {
                _error.value = "Error al sincronizar"
            } finally {
                _loading.value = false
            }
        }
    }

    fun createRoutine(name: String, timeMinutes: String) {
        val cleanName = name.trim()
        val cleanTime = timeMinutes.trim()

        if (cleanName.isBlank() || cleanTime.isBlank()) {
            _routineMessage.value = "Completa nombre y tiempo"
            return
        }

        viewModelScope.launch {
            _loading.value = true
            try {
                // Actualmente la tabla remota guarda 'name'; el tiempo se serializa en el nombre.
                repo.createRoutine("$cleanName (${cleanTime} min)")
                repo.syncPendingRoutines()
                _routineMessage.value = "Rutina creada"
            } catch (e: Exception) {
                _routineMessage.value = "No se pudo crear rutina"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearRoutineMessage() {
        _routineMessage.value = null
    }
}
