package com.example.atomic.domain.repository

import com.example.atomic.data.BlockedApp
import kotlinx.coroutines.flow.Flow

interface BlockedAppRepository {
    fun getBlockedPackages(): Flow<List<String>>
    suspend fun addBlockedApp(app: BlockedApp)
    suspend fun removeBlockedApp(packageName: String)
    suspend fun count(): Int
}
