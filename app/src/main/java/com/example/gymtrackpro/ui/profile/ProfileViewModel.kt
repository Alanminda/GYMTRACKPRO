/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/ui/profile/ProfileViewModel.kt
 * Proposito: Gestion de perfil y sesion del usuario.
 */
package com.example.gymtrackpro.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymtrackpro.data.local.entities.UserLocalEntity
import com.example.gymtrackpro.data.remote.dto.ProfileDto
import com.example.gymtrackpro.data.repository.GymRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val repo: GymRepository) : ViewModel() {

    // [Req C - MVVM] Estado observable del perfil.
    private val _session = MutableLiveData<UserLocalEntity?>(null)
    val session: LiveData<UserLocalEntity?> = _session

    private val _profile = MutableLiveData<ProfileDto?>(null)
    val profile: LiveData<ProfileDto?> = _profile

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    private val _loggedOut = MutableLiveData(false)
    val loggedOut: LiveData<Boolean> = _loggedOut

    fun loadProfile() {
        viewModelScope.launch {
            // [Req B] Consulta perfil remoto; [Req C] fallback de sesion local.
            _loading.value = true
            _error.value = null
            try {
                val session = repo.getSession()
                _session.value = session
                if (session != null) {
                    _profile.value = repo.fetchProfile()
                }
            } catch (_: Exception) {
                _error.value = "No se pudo cargar perfil"
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateProfile(name: String, email: String) {
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        if (cleanName.isBlank() || cleanEmail.isBlank()) {
            _message.value = "Completa nombre y email"
            return
        }

        viewModelScope.launch {
            // [Req B] Envio de datos de perfil a API.
            _loading.value = true
            _error.value = null
            try {
                val updated = repo.updateProfile(cleanName, cleanEmail)
                _profile.value = updated
                _session.value = repo.getSession()
                _message.value = "Perfil actualizado"
            } catch (_: Exception) {
                _message.value = "No se pudo actualizar perfil"
            } finally {
                _loading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // [Req A] Borra sesion local.
            repo.logout()
            _loggedOut.value = true
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

