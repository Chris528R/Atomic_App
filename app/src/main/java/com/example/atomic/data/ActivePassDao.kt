package com.example.atomic.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivePassDao {
    @Query("SELECT * FROM active_passes")
    fun getActivePasses(): Flow<List<ActivePass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPass(pass: ActivePass)

    @Query("DELETE FROM active_passes WHERE packageName = :packageName")
    suspend fun deletePass(packageName: String)

    @Query("DELETE FROM active_passes WHERE expiryTime <= :currentTime")
    suspend fun deleteExpiredPasses(currentTime: Long)
}
