package com.example.gymtrackpro.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gymtrackpro.data.repository.GymRepository
import com.example.gymtrackpro.ui.auth.AuthViewModel
import com.example.gymtrackpro.ui.community.CommunityRoutineDetailViewModel
import com.example.gymtrackpro.ui.community.CommunityViewModel
import com.example.gymtrackpro.ui.exercises.ExerciseDetailViewModel
import com.example.gymtrackpro.ui.exercises.ExercisesViewModel
import com.example.gymtrackpro.ui.home.HomeViewModel
import com.example.gymtrackpro.ui.profile.ProfileViewModel
import com.example.gymtrackpro.ui.routines.RoutineDetailViewModel
import com.example.gymtrackpro.ui.routines.RoutinePickerViewModel

class ViewModelFactory(private val repo: GymRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repo) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(repo) as T
            modelClass.isAssignableFrom(ExercisesViewModel::class.java) -> ExercisesViewModel(repo) as T
            modelClass.isAssignableFrom(CommunityViewModel::class.java) -> CommunityViewModel(repo) as T
            modelClass.isAssignableFrom(CommunityRoutineDetailViewModel::class.java) -> CommunityRoutineDetailViewModel(repo) as T
            modelClass.isAssignableFrom(ExerciseDetailViewModel::class.java) -> ExerciseDetailViewModel(repo) as T
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> ProfileViewModel(repo) as T
            modelClass.isAssignableFrom(RoutineDetailViewModel::class.java) -> RoutineDetailViewModel(repo) as T
            modelClass.isAssignableFrom(RoutinePickerViewModel::class.java) -> RoutinePickerViewModel(repo) as T
            else -> throw IllegalArgumentException("ViewModel no soportado")
        }
    }
}
