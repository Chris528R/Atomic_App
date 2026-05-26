package com.example.atomic.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.atomic.data.AtomicDatabase
import com.example.atomic.util.NotificationHelper
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

class HabitReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AtomicDatabase.getDatabase(applicationContext)
        val habits = database.proactiveHabitDao().getAllHabitsList()
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val startOfDayMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        for (habit in habits) {
            // Solo procesamos si es la hora configurada (o si queremos una ventana, pero el prompt dice "revisará la hora")
            // Como el Worker corre cada hora o periódicamente, revisamos si coincide la hora
            if (habit.triggerHour == currentHour) {
                if (habit.isPhysical) {
                    // Hábito físico: siempre notifica
                    NotificationHelper.showReminderNotification(
                        context = applicationContext,
                        habitId = habit.id,
                        title = "Recordatorio: ${habit.name}",
                        message = "¿Ya lo hiciste? ¡Es momento!"
                    )
                } else {
                    // Hábito digital: revisa UsageLog
                    val targetApp = habit.targetPackage ?: continue
                    val openCount = database.usageLogDao().getTodayOpenCount(targetApp, startOfDayMillis)

                    if (openCount == 0) {
                        NotificationHelper.showReminderNotification(
                            context = applicationContext,
                            habitId = habit.id,
                            title = "¡No rompas la racha!",
                            message = "Aún no has usado ${habit.name} hoy. ¿Tienes 10 minutos ahora?"
                        )
                    }
                }
            }
        }

        return Result.success()
    }
}
