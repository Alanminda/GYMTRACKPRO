/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/ui/auth/AuthViewModel.kt
 * Proposito: Pantallas y ViewModel de autenticacion (login/registro).
 */
package com.example.gymtrackpro.ui.auth

import androidx.lifecycle.*
import com.example.gymtrackpro.data.repository.GymRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: GymRepository) : ViewModel() {

    // [Req C - MVVM] Estado observable de UI.
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _loggedIn = MutableLiveData(false)
    val loggedIn: LiveData<Boolean> = _loggedIn

    fun checkSession() {
        // [Req C - Corrutinas] Consulta de sesion en segundo plano.
        viewModelScope.launch {
            _loggedIn.value = repo.getSession() != null
        }
    }

    fun login(email: String, password: String) {
        // [Req B + C] Llamada Retrofit via Repository + estado de carga/error.
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repo.login(email, password)
                _loggedIn.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Error en login"
            } finally {
                _loading.value = false
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        // [Req B + C] Alta remota y persistencia local de sesion.
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                repo.register(name, email, password)
                _loggedIn.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Error en registro"
            } finally {
                _loading.value = false
            }
        }
    }
}

