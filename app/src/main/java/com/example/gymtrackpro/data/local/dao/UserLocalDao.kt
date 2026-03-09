/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/data/local/dao/UserLocalDao.kt
 * Proposito: Define operaciones de acceso a datos (DAO) para Room.
 */
package com.example.gymtrackpro.data.local.dao

import androidx.room.*
import com.example.gymtrackpro.data.local.entities.UserLocalEntity

@Dao
interface UserLocalDao {

    // [Req A - Read] Lee sesion persistida para mantener login entre reinicios.
    @Query("SELECT * FROM user_local WHERE id = 1 LIMIT 1")
    suspend fun getSession(): UserLocalEntity?

    // [Req A - Create/Update] Crea o actualiza sesion local.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(user: UserLocalEntity)

    // [Req A - Delete] Limpia sesion en logout.
    @Query("DELETE FROM user_local")
    suspend fun clearSession()
}

