package com.example.atomic.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleRuleDao {
    @Query("SELECT * FROM schedule_rules")
    fun getAllRules(): Flow<List<ScheduleRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: ScheduleRule)

    @Update
    suspend fun updateRule(rule: ScheduleRule)

    @Delete
    suspend fun deleteRule(rule: ScheduleRule)
}
