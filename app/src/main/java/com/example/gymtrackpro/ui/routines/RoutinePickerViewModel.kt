/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/ui/routines/RoutinePickerViewModel.kt
 * Proposito: Detalle/seleccion de rutinas y acciones sobre ejercicios internos.
 */
package com.example.gymtrackpro.ui.routines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackpro.data.local.entities.RoutineEntity
import com.example.gymtrackpro.data.repository.GymRepository
import kotlinx.coroutines.launch

class RoutinePickerViewModel(private val repo: GymRepository) : ViewModel() {

    private val _routines = MutableLiveData<List<RoutineEntity>>(emptyList())
    val routines: LiveData<List<RoutineEntity>> = _routines

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun loadRoutines() {
        // [Req C - Corrutinas] Carga en segundo plano.
        // [Req A - Read] Obtiene rutinas desde Room a traves del Repository.
        viewModelScope.launch {
            try {
                _routines.value = repo.getRoutinesLocal()
            } catch (_: Exception) {
                _error.value = "No se pudieron cargar las rutinas"
            }
        }
    }
}

