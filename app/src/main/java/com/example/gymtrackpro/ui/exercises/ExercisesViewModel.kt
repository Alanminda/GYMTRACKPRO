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

    fun loadExercises() {
        resetAndLoad()
    }

    fun onQueryChanged(text: String) {
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
        offset = 0
        endReached = false
        _exercises.value = emptyList()
        loadNextPage()
    }

    private fun loadNextPage() {
        if (loadingPage || endReached) return
        val currentGeneration = generation

        loadJob = viewModelScope.launch {
            loadingPage = true
            _loading.value = true
            _error.value = null
            try {
                val page = repo.fetchExercisesPageFromApi(
                    query = query.takeIf { it.isNotBlank() },
                    limit = PAGE_SIZE,
                    offset = offset
                )

                if (currentGeneration != generation) return@launch

                if (page.size < PAGE_SIZE) {
                    endReached = true
                }

                val merged = _exercises.value.orEmpty() + page
                _exercises.value = merged.distinctBy { it.id }
                offset += page.size
            } catch (_: CancellationException) {
                // Se cancela cuando cambia la busqueda o se reinicia paginacion.
            } catch (e: Exception) {
                if (currentGeneration != generation) return@launch
                _error.value = "No se pudieron cargar ejercicios"
            } finally {
                if (currentGeneration != generation) return@launch
                loadingPage = false
                _loading.value = false
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 80
    }
}
