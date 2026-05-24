package com.example.atomic.data.repository

import com.example.atomic.data.ActivePass
import com.example.atomic.data.ActivePassDao
import com.example.atomic.domain.repository.ActivePassRepository
import kotlinx.coroutines.flow.Flow

class ActivePassRepositoryImpl(
    private val dao: ActivePassDao
) : ActivePassRepository {
    override fun getActivePasses(): Flow<List<ActivePass>> {
        return dao.getActivePasses()
    }

    override suspend fun insertPass(pass: ActivePass) {
        dao.insertPass(pass)
    }

    override suspend fun deletePass(packageName: String) {
        dao.deletePass(packageName)
    }

    override suspend fun deleteExpiredPasses(currentTime: Long) {
        dao.deleteExpiredPasses(currentTime)
    }
}
