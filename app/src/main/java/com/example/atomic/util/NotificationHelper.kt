package com.example.atomic.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.atomic.service.HabitActionReceiver

object NotificationHelper {
    private const val CHANNEL_ID = "atomic_habits_channel"

    fun showReminderNotification(context: Context, habitId: Int, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Recordatorios de Hábitos",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val notificationId = habitId + 1000 // Simple unique ID for each habit

        val doneIntent = Intent(context, HabitActionReceiver::class.java).apply {
            action = HabitActionReceiver.ACTION_HABIT_DONE
            putExtra("habitId", habitId)
            putExtra("notificationId", notificationId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context, habitId, doneIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val laterIntent = Intent(context, HabitActionReceiver::class.java).apply {
            action = HabitActionReceiver.ACTION_HABIT_LATER
            putExtra("habitId", habitId)
            putExtra("notificationId", notificationId)
        }
        val laterPendingIntent = PendingIntent.getBroadcast(
            context, habitId + 100, laterIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la notificación
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(0, "¡Hecho!", donePendingIntent)
            .addAction(0, "Más tarde", laterPendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun testNotification(context: Context) {
        showReminderNotification(
            context,
            habitId = 999, // Dummy ID
            "Prueba de Notificación",
            "Si puedes ver esto, las notificaciones están funcionando correctamente."
        )
    }
}
