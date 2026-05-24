package com.example.atomic.data.repository

import com.example.atomic.data.BlockedApp
import com.example.atomic.data.BlockedAppDao
import com.example.atomic.domain.repository.BlockedAppRepository
import kotlinx.coroutines.flow.Flow

class BlockedAppRepositoryImpl(
    private val dao: BlockedAppDao
) : BlockedAppRepository {
    override fun getBlockedPackages(): Flow<List<String>> {
        return dao.getBlockedPackages()
    }

    override suspend fun addBlockedApp(app: BlockedApp) {
        dao.addBlockedApp(app)
    }

    override suspend fun removeBlockedApp(packageName: String) {
        dao.removeBlockedApp(packageName)
    }

    override suspend fun count(): Int {
        return dao.count()
    }
}
