package com.example.atomic.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.atomic.data.AtomicDatabase
import com.example.atomic.util.NotificationHelper
import java.time.LocalDate
import java.time.ZoneId

class HabitReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AtomicDatabase.getDatabase(applicationContext)
        val startOfDayMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Revisamos si abriste la app de programación hoy
        val targetApp = "com.sololearn" // Esto puede ser dinámico luego
        val openCount = database.usageLogDao().getTodayOpenCount(targetApp, startOfDayMillis)

        if (openCount == 0) {
            // ¡No hay acción! Lanzamos la notificación
            NotificationHelper.showReminderNotification(
                context = applicationContext,
                title = "¡No rompas la racha!",
                message = "Aún no has practicado programación hoy. ¿Tienes 10 minutos ahora?"
            )
        }

        return Result.success()
    }
}
