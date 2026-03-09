/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/ui/exercises/ExercisesViewModel.kt
 * Proposito: Exploracion y detalle de ejercicios, incluyendo agregar a rutina.
 */
package com.example.gymtrackpro.ui.exercises

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackpro.data.local.entities.ExerciseEntity
import com.example.gymtrackpro.data.repository.GymRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ExercisesViewModel(private val repo: GymRepository) : ViewModel() {

    // [Req C - MVVM] Estados que la UI observa (lista/carga/error).
    private val _exercises = MutableLiveData<List<ExerciseEntity>>(emptyList())
    val exercises: LiveData<List<ExerciseEntity>> = _exercises

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private var query: String = ""
    private var offset: Int = 0
    private var endReached: Boolean = false
    private var loadingPage: Boolean = false
    private var searchJob: Job? = null
    private var loadJob: Job? = null
    private var generation: Int = 0
    private var selectionMode: Boolean = false
    private var localOnlyMode: Boolean = false
    private var guestMode: Boolean = false

    fun setSelectionMode(enabled: Boolean) {
        selectionMode = enabled
    }

    fun loadExercises() {
        // [Req C - Corrutinas] Detecta sesion sin bloquear UI.
        viewModelScope.launch {
            guestMode = !repo.isLoggedIn()
            resetAndLoad()
        }
    }

    fun onQueryChanged(text: String) {
        // [Req B] Filtrado remoto/local con debounce para evitar sobrecarga de red.
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            query = text.trim()
            resetAndLoad()
        }
    }

    fun loadMoreIfNeeded(lastVisiblePosition: Int) {
        val currentSize = _exercises.value.orEmpty().size
        if (currentSize == 0) return
        if (endReached || loadingPage) return
        if (lastVisiblePosition < currentSize - 5) return
        loadNextPage()
    }

    private fun resetAndLoad() {
        generation += 1
        loadJob?.cancel()
        loadingPage = false
        _loading.value = false
        localOnlyMode = false
        offset = 0
        endReached = false
        _exercises.value = emptyList()
        loadNextPage()
    }

    private fun loadNextPage() {
        if (loadingPage || endReached) return
        val currentGeneration = generation

        loadJob = viewModelScope.launch {
            // [Req B] Carga paginada de API (listar/filtrar).
            // [Req C] Fallback local segun modo (invitado offline / seleccion de rutina).
            loadingPage = true
            _loading.value = true
            _error.value = null
            try {
                val page = if (localOnlyMode) {
                    if (selectionMode) {
                        repo.fetchExercisesPageFromLocalRoutineCache(
                            query = query.takeIf { it.isNotBlank() },
                            limit = currentPageSize(),
                            offset = offset
                        )
                    } else {
                        repo.fetchExercisesPageFromLocalCache(
                            query = query.takeIf { it.isNotBlank() },
                            limit = currentPageSize(),
                            offset = offset
                        )
                    }
                } else {
                    try {
                        repo.fetchExercisesPageFromApi(
                            query = query.takeIf { it.isNotBlank() },
                            limit = currentPageSize(),
                            offset = offset
                        )
                    } catch (e: Exception) {
                        if (!selectionMode && !guestMode) throw e
                        localOnlyMode = true
                        if (selectionMode) {
                            repo.fetchExercisesPageFromLocalRoutineCache(
                                query = query.takeIf { it.isNotBlank() },
                                limit = currentPageSize(),
                                offset = offset
                            )
                        } else {
                            repo.fetchExercisesPageFromLocalCache(
                                query = query.takeIf { it.isNotBlank() },
                                limit = currentPageSize(),
                                offset = offset
                            )
                        }
                    }
                }

                if (currentGeneration != generation) return@launch

                if (localOnlyMode) {
                    // [Req B] Mensajes de estado de red para UX offline.
                    _error.value = when {
                        selectionMode && offset == 0 && page.isEmpty() ->
                            "Sin red: no hay ejercicios locales disponibles para anadir"
                        selectionMode ->
                            "Sin red: usando ejercicios locales de tus rutinas"
                        guestMode && offset == 0 && page.isEmpty() ->
                            "Modo invitado sin red: no hay cache local de ejercicios"
                        guestMode ->
                            "Modo invitado sin red: mostrando cache local de ejercicios"
                        else -> null
                    }
                }

                if (page.isEmpty()) {
                    endReached = true
                }

                val merged = _exercises.value.orEmpty() + page
                _exercises.value = merged.distinctBy { it.id }
                offset += page.size
            } catch (_: CancellationException) {
                // Se cancela cuando cambia la busqueda o se reinicia paginacion.
            } catch (_: Exception) {
                if (currentGeneration != generation) return@launch
                _error.value = when {
                    guestMode -> "Modo invitado online no disponible temporalmente"
                    selectionMode -> "No se pudieron cargar ejercicios para anadir"
                    else -> "No se pudieron cargar ejercicios"
                }
            } finally {
                if (currentGeneration != generation) return@launch
                loadingPage = false
                _loading.value = false
            }
        }
    }

    companion object {
        private const val INITIAL_PAGE_SIZE = 10
        private const val NEXT_PAGE_SIZE = 50
    }

    private fun currentPageSize(): Int {
        return if (offset == 0) INITIAL_PAGE_SIZE else NEXT_PAGE_SIZE
    }
}

