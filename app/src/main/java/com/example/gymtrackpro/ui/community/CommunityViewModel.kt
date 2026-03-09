package com.example.gymtrackpro.ui.community

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackpro.data.remote.dto.CommunityRoutineDto
import com.example.gymtrackpro.data.repository.GymRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CommunityViewModel(private val repo: GymRepository) : ViewModel() {

    private val _items = MutableLiveData<List<CommunityRoutineDto>>(emptyList())
    val items: LiveData<List<CommunityRoutineDto>> = _items

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    private var loadingPage: Boolean = false
    private var endReached: Boolean = false
    private var offset: Int = 0
    private var query: String = ""
    private var sort: String = "recent"
    private var searchJob: Job? = null
    private var generation: Int = 0

    fun loadInitial() {
        generation += 1
        offset = 0
        endReached = false
        loadingPage = false
        _items.value = emptyList()
        _loading.value = false
        loadNextPage()
    }

    fun onQueryChanged(text: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            query = text.trim()
            loadInitial()
        }
    }

    fun onSortChanged(value: String) {
        val normalized = value.trim().lowercase()
        if (normalized == sort) return
        sort = normalized
        loadInitial()
    }

    fun loadMoreIfNeeded(lastVisiblePosition: Int) {
        val count = _items.value.orEmpty().size
        if (count == 0 || loadingPage || endReached) return
        if (lastVisiblePosition < count - 4) return
        loadNextPage()
    }

    private fun loadNextPage() {
        if (loadingPage || endReached) return
        val currentGeneration = generation
        loadingPage = true
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val page = repo.fetchCommunityRoutinesPage(
                    limit = currentPageSize(),
                    offset = offset,
                    query = query,
                    sort = sort
                )
                if (currentGeneration != generation) return@launch
                if (page.isEmpty()) endReached = true
                _items.value = (_items.value.orEmpty() + page).distinctBy { it.id }
                offset += page.size
            } catch (_: CancellationException) {
                // Cambios de busqueda/filtro cancelan carga anterior.
            } catch (_: Exception) {
                if (currentGeneration != generation) return@launch
                _error.value = "No se pudo cargar comunidad"
            } finally {
                loadingPage = false
                if (currentGeneration == generation) {
                    _loading.value = false
                }
            }
        }
    }

    fun toggleFavorite(item: CommunityRoutineDto) {
        viewModelScope.launch {
            try {
                val newState = !item.isFavorite
                val remote = repo.setCommunityRoutineFavorite(item.id, newState)
                _items.value = _items.value.orEmpty().map {
                    if (it.id == item.id) {
                        it.copy(
                            isFavorite = remote.isFavorite,
                            favoritesCount = remote.favoritesCount
                        )
                    } else {
                        it
                    }
                }
                _message.value = if (remote.isFavorite) "Anadida a favoritos" else "Quitada de favoritos"
            } catch (_: Exception) {
                _message.value = "No se pudo actualizar favorito"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun currentPageSize(): Int {
        return if (offset == 0) 10 else 20
    }
}
