package com.example.atomic.util

import android.content.Context
import android.net.Uri
import com.example.atomic.data.AtomicDatabase
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object BackupHelper {
    private val gson = GsonBuilder().create()

    suspend fun exportDataToJson(context: Context, uri: Uri, database: AtomicDatabase) = withContext(Dispatchers.IO) {
        val backupData = mutableMapOf<String, Any>()
        
        backupData["blocked_apps"] = database.blockedAppDao().getBlockedPackages().first()
        backupData["schedule_rules"] = database.scheduleRuleDao().getAllRules().first()
        backupData["proactive_habits"] = database.proactiveHabitDao().getAllHabits().first()
        backupData["habit_replacements"] = database.habitReplacementDao().getAllReplacements().first()
        backupData["time_debt"] = database.timeDebtDao().getDebt() ?: 0

        val jsonString = gson.toJson(backupData)

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                writer.write(jsonString)
            }
        }
    }

    suspend fun importDataFromJson(context: Context, uri: Uri, database: AtomicDatabase) = withContext(Dispatchers.IO) {
        val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                reader.readText()
            }
        } ?: return@withContext

        // Basic map parse
        val type = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
        val backupData: Map<String, Any> = gson.fromJson(jsonString, type)

        // As it's just MVP, we clear and restore what we can safely:
        // Schedule Rules
        if (backupData.containsKey("schedule_rules")) {
            val rulesStr = gson.toJson(backupData["schedule_rules"])
            val rulesType = object : com.google.gson.reflect.TypeToken<List<com.example.atomic.data.ScheduleRule>>() {}.type
            val rules: List<com.example.atomic.data.ScheduleRule> = gson.fromJson(rulesStr, rulesType)
            // Just add them (we could clear first but append is safer for now)
            rules.forEach { database.scheduleRuleDao().insertRule(it.copy(id = 0)) }
        }

        // Proactive Habits
        if (backupData.containsKey("proactive_habits")) {
            val habitsStr = gson.toJson(backupData["proactive_habits"])
            val habitsType = object : com.google.gson.reflect.TypeToken<List<com.example.atomic.data.ProactiveHabit>>() {}.type
            val habits: List<com.example.atomic.data.ProactiveHabit> = gson.fromJson(habitsStr, habitsType)
            habits.forEach { database.proactiveHabitDao().insertHabit(it.copy(id = 0)) }
        }

        // Habit Replacements
        if (backupData.containsKey("habit_replacements")) {
            val repStr = gson.toJson(backupData["habit_replacements"])
            val repType = object : com.google.gson.reflect.TypeToken<List<com.example.atomic.data.HabitReplacement>>() {}.type
            val replacements: List<com.example.atomic.data.HabitReplacement> = gson.fromJson(repStr, repType)
            replacements.forEach { database.habitReplacementDao().insertReplacement(it) }
        }
    }
}
