package com.example.atomic.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UsageLog::class, BlockedApp::class, ActivePass::class, TimeDebt::class, ScheduleRule::class, HabitReplacement::class, ProactiveHabit::class],
    version = 9,
    exportSchema = false,
)
abstract class AtomicDatabase : RoomDatabase() {
    abstract fun usageLogDao(): UsageLogDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun activePassDao(): ActivePassDao
    abstract fun timeDebtDao(): TimeDebtDao
    abstract fun scheduleRuleDao(): ScheduleRuleDao
    abstract fun habitReplacementDao(): HabitReplacementDao
    abstract fun proactiveHabitDao(): ProactiveHabitDao

    companion object {
        @Volatile
        private var INSTANCE: AtomicDatabase? = null

        fun getDatabase(context: Context): AtomicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AtomicDatabase::class.java,
                    "atomic_database",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
