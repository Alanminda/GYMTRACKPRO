package com.example.gymtrackpro.data.remote.dto

data class RoutineUpsertRequest(
    val name: String
)

data class RoutineExerciseSyncRequest(
    val exerciseIds: List<String>
)

data class RoutineRemoteDto(
    val _id: String,
    val name: String,
    val exerciseIds: List<String> = emptyList()
)

data class CommunityRoutineDto(
    val id: String,
    val name: String,
    val ownerName: String,
    val exerciseIds: List<String> = emptyList(),
    val exerciseCount: Int = 0,
    val durationMinutes: Int? = null,
    val favoritesCount: Int = 0,
    val isFavorite: Boolean = false
)

data class CommunityFavoriteToggleDto(
    val message: String,
    val favoritesCount: Int = 0,
    val isFavorite: Boolean = false
)
