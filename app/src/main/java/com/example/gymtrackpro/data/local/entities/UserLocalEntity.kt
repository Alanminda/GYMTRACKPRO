package com.example.gymtrackpro.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_local")
data class UserLocalEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val token: String
)
