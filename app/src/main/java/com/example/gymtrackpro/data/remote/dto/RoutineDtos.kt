/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/data/remote/dto/RoutineDtos.kt
 * Proposito: Modelos DTO para serializacion/deserializacion de API.
 */
package com.example.gymtrackpro.data.remote.dto

data class RoutineUpsertRequest(
    // Nombre de rutina (incluye duracion en texto si aplica).
    val name: String
)

data class RoutineExerciseSyncRequest(
    // Conjunto final de ejercicios de la rutina (sync por reemplazo).
    val exerciseIds: List<String>
)

data class RoutineRemoteDto(
    // Id remoto de rutina.
    val _id: String,
    val name: String,
    // Relacion N:M serializada como lista de ids.
    val exerciseIds: List<String> = emptyList()
)

data class CommunityRoutineDto(
    // Id de rutina publica.
    val id: String,
    val name: String,
    val ownerName: String,
    val exerciseIds: List<String> = emptyList(),
    // Metadatos calculados para tarjetas/filtros.
    val exerciseCount: Int = 0,
    val durationMinutes: Int? = null,
    val alreadyShared: Boolean = false,
    val favoritesCount: Int = 0,
    val isFavorite: Boolean = false
)

data class CommunityFavoriteToggleDto(
    // Mensaje operativo del backend.
    val message: String,
    val favoritesCount: Int = 0,
    val isFavorite: Boolean = false
)

