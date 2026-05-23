package com.example.atomic.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UsageLog::class], version = 1, exportSchema = false)
abstract class AtomicDatabase : RoomDatabase() {
    abstract fun usageLogDao(): UsageLogDao

    companion object {
        @Volatile
        private var INSTANCE: AtomicDatabase? = null

        fun getDatabase(context: Context): AtomicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AtomicDatabase::class.java,
                    "atomic_database",
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
