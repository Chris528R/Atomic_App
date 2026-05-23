package com.example.atomic.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.atomic.data.AtomicDatabase
import com.example.atomic.data.UsageLog
import com.example.atomic.data.repository.ActivePassRepositoryImpl
import com.example.atomic.data.repository.BlockedAppRepositoryImpl
import com.example.atomic.data.repository.UsageRepositoryImpl
import com.example.atomic.domain.repository.ActivePassRepository
import com.example.atomic.domain.repository.BlockedAppRepository
import com.example.atomic.domain.repository.UsageRepository
import com.example.atomic.overlay.WindowOverlayManager
import com.example.atomic.util.resolveAppDisplayName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AppTrackerService : AccessibilityService() {

    private lateinit var overlayManager: WindowOverlayManager
    private lateinit var blockedAppRepository: BlockedAppRepository
    private lateinit var activePassRepository: ActivePassRepository
    private lateinit var usageRepository: UsageRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    /** Paquete → instante de expiración del pase (epoch millis). */
    private val unlockedApps = mutableMapOf<String, Long>()

    @Volatile
    private var dynamicBlockedApps: List<String> = emptyList()

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = WindowOverlayManager(applicationContext)
        val database = AtomicDatabase.getDatabase(applicationContext)
        blockedAppRepository = BlockedAppRepositoryImpl(database.blockedAppDao())
        activePassRepository = ActivePassRepositoryImpl(database.activePassDao())
        usageRepository = UsageRepositoryImpl(database.usageLogDao())

        serviceScope.launch {
            blockedAppRepository.getBlockedPackages().collect { packages ->
                dynamicBlockedApps = packages
            }
        }

        serviceScope.launch {
            activePassRepository.getActivePasses().collect { passes ->
                val currentTime = System.currentTimeMillis()
                val active = passes.filter { it.expiryTime > currentTime }

                unlockedApps.clear()
                active.forEach { unlockedApps[it.packageName] = it.expiryTime }

                if (passes.size > active.size) {
                    activePassRepository.deleteExpiredPasses(currentTime)
                }
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == applicationContext.packageName) return

        if (!dynamicBlockedApps.contains(packageName)) return

        val currentTime = System.currentTimeMillis()
        val expiryTime = unlockedApps[packageName] ?: 0L

        if (currentTime < expiryTime) {
            return
        }

        if (expiryTime > 0L) {
            unlockedApps.remove(packageName)
            serviceScope.launch {
                activePassRepository.deletePass(packageName)
            }
        }

        overlayManager.showFrictionScreen(
            appName = resolveAppDisplayName(packageName),
            onUnlock = { reason ->
                val durationMin = 15
                val unlockTime = System.currentTimeMillis()
                val passExpiry = unlockTime + durationMin * 60 * 1000L
                unlockedApps[packageName] = passExpiry

                serviceScope.launch {
                    activePassRepository.insertPass(com.example.atomic.data.ActivePass(packageName, passExpiry))
                    usageRepository.insertLog(
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
