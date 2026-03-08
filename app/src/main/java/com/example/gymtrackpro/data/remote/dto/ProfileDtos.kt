package com.example.gymtrackpro.data.remote.dto

data class ProfileDto(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: String?
)

data class ProfileUpdateRequest(
    val name: String,
    val email: String
)
