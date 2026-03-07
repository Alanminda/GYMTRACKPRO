package com.example.gymtrackpro.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymtrackpro.data.repository.GymRepository
import com.example.gymtrackpro.ui.auth.AuthViewModel
import com.example.gymtrackpro.ui.exercises.ExercisesViewModel
import com.example.gymtrackpro.ui.home.HomeViewModel

class ViewModelFactory(private val repo: GymRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repo) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repo) as T
            modelClass.isAssignableFrom(ExercisesViewModel::class.java) -> ExercisesViewModel(repo) as T
            else -> throw IllegalArgumentException("ViewModel no soportado")
        }
    }
}
