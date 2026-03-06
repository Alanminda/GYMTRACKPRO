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
