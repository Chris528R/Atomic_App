package com.example.atomic.ui

import com.example.atomic.data.UsageLog
import com.example.atomic.domain.repository.UsageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UsageViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeUsageRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeUsageRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when viewmodel initializes, it loads logs from repository`() = runTest {
        val testLogs = listOf(
            UsageLog(id = 1, packageName = "com.test.app1", reason = "Testing", timestamp = 1000L, durationMinutes = 15),
            UsageLog(id = 2, packageName = "com.test.app2", reason = "Testing 2", timestamp = 2000L, durationMinutes = 15)
        )
        fakeRepository.emit(testLogs)

        val viewModel = UsageViewModel(fakeRepository)
        
        // Wait for coroutines to complete execution
        testScheduler.advanceUntilIdle()

        assertEquals(testLogs, viewModel.logs.value)
    }

    class FakeUsageRepository : UsageRepository {
        private val _logsFlow = MutableStateFlow<List<UsageLog>>(emptyList())

        suspend fun emit(logs: List<UsageLog>) {
            _logsFlow.value = logs
        }

        override fun getAllLogs(): Flow<List<UsageLog>> {
            return _logsFlow
        }

        override suspend fun insertLog(log: UsageLog): Long {
            _logsFlow.value = _logsFlow.value + log
            return log.id
        }

        override suspend fun updateUsageEnd(logId: Long, closeTime: Long, usage: Long) {
            val logs = _logsFlow.value.toMutableList()
            val index = logs.indexOfFirst { it.id == logId }
            if (index != -1) {
                val oldLog = logs[index]
                logs[index] = oldLog.copy(closeTimestamp = closeTime, realUsageMillis = usage)
                _logsFlow.value = logs
            }
        }
    }
}
