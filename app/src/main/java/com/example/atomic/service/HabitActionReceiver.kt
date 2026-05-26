package com.example.atomic.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.atomic.data.AtomicDatabase
import com.example.atomic.data.HabitCompletion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HabitActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getIntExtra("habitId", -1)
        val notificationId = intent.getIntExtra("notificationId", 1001)
        val action = intent.action

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId) // Cierra la notificación

        if (habitId != -1 && action == ACTION_HABIT_DONE) {
            val database = AtomicDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                val today = java.time.LocalDate.now().toString()
                val existing = database.habitCompletionDao().isCompleted(habitId, today)
                if (existing == 0) {
                    database.habitCompletionDao().insertCompletion(
                        HabitCompletion.createForToday(habitId)
                    )
                }
            }
        }
    }

    companion object {
        const val ACTION_HABIT_DONE = "com.example.atomic.ACTION_HABIT_DONE"
        const val ACTION_HABIT_LATER = "com.example.atomic.ACTION_HABIT_LATER"
    }
}
