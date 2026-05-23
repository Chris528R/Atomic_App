package com.example.atomic.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.atomic.data.AtomicDatabase
import com.example.atomic.data.UsageLog
import com.example.atomic.overlay.WindowOverlayManager
import com.example.atomic.util.resolveAppDisplayName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AppTrackerService : AccessibilityService() {

    private lateinit var overlayManager: WindowOverlayManager
    private lateinit var database: AtomicDatabase

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    /** Paquete → instante de expiración del pase (epoch millis). */
    private val unlockedApps = mutableMapOf<String, Long>()

    private val blockedApps = setOf(
        "com.instagram.android",
        "com.facebook.katana",
        "com.google.android.youtube",
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = WindowOverlayManager(applicationContext)
        database = AtomicDatabase.getDatabase(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName) return

        if (!blockedApps.contains(packageName)) return

        val currentTime = System.currentTimeMillis()
        val expiryTime = unlockedApps[packageName] ?: 0L

        if (currentTime < expiryTime) {
            return
        }

        if (expiryTime > 0L) {
            unlockedApps.remove(packageName)
        }

        overlayManager.showFrictionScreen(
            appName = resolveAppDisplayName(packageName),
            onUnlock = { reason ->
                val durationMin = 15
                val unlockTime = System.currentTimeMillis()
                unlockedApps[packageName] = unlockTime + durationMin * 60 * 1000L

                serviceScope.launch {
                    database.usageLogDao().insertLog(
                        UsageLog(
                            packageName = packageName,
                            reason = reason,
                            timestamp = unlockTime,
                            durationMinutes = durationMin,
                        ),
                    )
                }
            },
            onCancel = {
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(homeIntent)
            },
        )
    }

    override fun onInterrupt() {
        overlayManager.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (::overlayManager.isInitialized) {
            overlayManager.dismiss()
        }
    }

}
