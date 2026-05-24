package com.example.atomic.domain.repository

import com.example.atomic.data.ActivePass
import kotlinx.coroutines.flow.Flow

interface ActivePassRepository {
    fun getActivePasses(): Flow<List<ActivePass>>
    suspend fun insertPass(pass: ActivePass)
    suspend fun deletePass(packageName: String)
    suspend fun deleteExpiredPasses(currentTime: Long)
}
