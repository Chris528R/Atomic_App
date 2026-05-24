package com.example.atomic.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "habit_replacements")
data class HabitReplacement(
    @PrimaryKey val blockedPackageName: String,
    val replacementPackageName: String,
    val replacementAppName: String
)

@Dao
interface HabitReplacementDao {
    @Query("SELECT * FROM habit_replacements")
    fun getAllReplacements(): Flow<List<HabitReplacement>>

    @Query("SELECT * FROM habit_replacements WHERE blockedPackageName = :blockedPkg")
    suspend fun getReplacementForApp(blockedPkg: String): HabitReplacement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplacement(replacement: HabitReplacement)

    @Query("DELETE FROM habit_replacements WHERE blockedPackageName = :blockedPkg")
    suspend fun deleteReplacement(blockedPkg: String)
}
