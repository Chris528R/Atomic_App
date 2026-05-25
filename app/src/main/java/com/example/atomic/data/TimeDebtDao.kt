package com.example.atomic.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TimeDebtDao {
    @Query("SELECT debtMinutes FROM time_debt WHERE id = 1")
    fun getDebtFlow(): kotlinx.coroutines.flow.Flow<Int?>

    @Query("SELECT debtMinutes FROM time_debt WHERE id = 1")
    suspend fun getDebt(): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun initDebt(debt: TimeDebt = TimeDebt(1, 0))

    @Query("UPDATE time_debt SET debtMinutes = debtMinutes + :minutes WHERE id = 1")
    suspend fun addDebt(minutes: Int)

    @Query("UPDATE time_debt SET debtMinutes = MAX(0, debtMinutes - :minutes) WHERE id = 1")
    suspend fun reduceDebt(minutes: Int)
}
