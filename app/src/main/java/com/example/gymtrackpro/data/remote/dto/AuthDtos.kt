package com.example.gymtrackpro.data.remote.dto

data class RegisterRequest(val name: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val token: String, val name: String, val email: String)
