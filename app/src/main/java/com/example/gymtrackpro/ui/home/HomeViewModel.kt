package com.example.gymtrackpro.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.gymtrackpro.data.local.entities.RoutineEntity
import com.example.gymtrackpro.data.repository.GymRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: GymRepository) : ViewModel() {

    private val ownRoutines = repo.observeRoutinesLocal().asLiveData()
    private val favoriteRoutines = repo.observeFavoriteRoutinesLocal().asLiveData()
    private val _category = MutableLiveData(RoutineCategory.OWN)
    val category: LiveData<RoutineCategory> = _category

    private val _routines = MediatorLiveData<List<RoutineEntity>>(emptyList())
    val routines: LiveData<List<RoutineEntity>> = _routines

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _routineMessage = MutableLiveData<String?>(null)
    val routineMessage: LiveData<String?> = _routineMessage

    init {
        _routines.addSource(ownRoutines) { refreshCategoryList() }
        _routines.addSource(favoriteRoutines) { refreshCategoryList() }
        _routines.addSource(_category) { refreshCategoryList() }
    }

    fun syncData() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repo.syncForLoggedUser()
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
                repo.createRoutine("$cleanName (${cleanTime} min)")
                val loggedIn = repo.isLoggedIn()
                if (!loggedIn) {
                    _routineMessage.value = "Rutina creada en local (modo invitado)"
                } else {
                    try {
                        repo.syncForLoggedUser()
                        _routineMessage.value = if (repo.hasPendingRoutineSync()) {
                            "Rutina creada local; sync pendiente"
                        } else {
                            "Rutina creada y sincronizada"
                        }
                    } catch (_: Exception) {
                        _routineMessage.value = "Rutina creada local; sync pendiente"
                    }
                }
            } catch (_: Exception) {
                _routineMessage.value = "No se pudo crear rutina"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearRoutineMessage() {
        _routineMessage.value = null
    }

    fun setCategory(category: RoutineCategory) {
        _category.value = category
    }

    fun deleteRoutine(routineId: Int) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repo.deleteRoutine(routineId)
                if (repo.isLoggedIn()) {
                    try {
                        repo.syncForLoggedUser()
                        _routineMessage.value = if (repo.hasPendingRoutineSync()) {
                            "Rutina eliminada local; sync pendiente"
                        } else {
                            "Rutina eliminada"
                        }
                    } catch (_: Exception) {
                        _routineMessage.value = "Rutina eliminada local; sync pendiente"
                    }
                } else {
                    _routineMessage.value = "Rutina eliminada"
                }
            } catch (_: Exception) {
                _routineMessage.value = "No se pudo eliminar la rutina"
            } finally {
                _loading.value = false
            }
        }
    }

    fun shareRoutine(routineId: Int, routineName: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                if (!repo.isLoggedIn()) {
                    _routineMessage.value = "Inicia sesion para compartir"
                    return@launch
                }
                val shared = repo.shareRoutine(routineId)
                _routineMessage.value = if (shared.alreadyShared) {
                    "Ya estaba compartida, se actualizo: $routineName"
                } else {
                    "Rutina compartida: $routineName"
                }
            } catch (_: Exception) {
                _routineMessage.value = "No se pudo compartir la rutina"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun refreshCategoryList() {
        val selected = _category.value ?: RoutineCategory.OWN
        _routines.value = when (selected) {
            RoutineCategory.OWN -> ownRoutines.value.orEmpty()
            RoutineCategory.FAVORITES -> favoriteRoutines.value.orEmpty()
        }
    }

    enum class RoutineCategory {
        OWN,
        FAVORITES
    }
}
