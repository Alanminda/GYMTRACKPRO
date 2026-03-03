package com.example.gymtrackpro.data.local.dao

import androidx.room.*
import com.example.gymtrackpro.data.local.entities.UserLocalEntity

@Dao
interface UserLocalDao {

    @Query("SELECT * FROM user_local WHERE id = 1 LIMIT 1")
    suspend fun getSession(): UserLocalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(user: UserLocalEntity)

    @Query("DELETE FROM user_local")
    suspend fun clearSession()
}
