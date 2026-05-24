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

    // Estado de la sesión actual
    private var currentActiveLogId: Long? = null
    private var currentActivePackage: String? = null
    private var currentSessionStartTime: Long = 0L

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

        // 1. Verificar si salimos de la app activa
        if (currentActivePackage != null && packageName != currentActivePackage) {
            // Filtramos eventos del sistema que no son cierres reales
            if (packageName != "com.android.systemui" && !packageName.contains("inputmethod")) {
                val closeTime = System.currentTimeMillis()
                val usage = closeTime - currentSessionStartTime
                val logIdToUpdate = currentActiveLogId

                // Limpiar estado en memoria
                currentActiveLogId = null
                currentActivePackage = null
                currentSessionStartTime = 0L

                // Actualizar Room en segundo plano
                if (logIdToUpdate != null) {
                    serviceScope.launch {
                        usageRepository.updateUsageEnd(logIdToUpdate, closeTime, usage)
                    }
                }
            }
        }

        if (packageName == applicationContext.packageName) return

        if (!dynamicBlockedApps.contains(packageName)) return

        val currentTime = System.currentTimeMillis()
        val expiryTime = unlockedApps[packageName] ?: 0L

        if (currentTime < expiryTime) {
            if (currentActivePackage != packageName) {
                val startTime = System.currentTimeMillis()
                serviceScope.launch {
                    val insertedId = usageRepository.insertLog(
                        UsageLog(
                            packageName = packageName,
                            reason = "Pase activo",
                            timestamp = startTime,
                            durationMinutes = 15
                        )
                    )
                    currentActiveLogId = insertedId
                    currentActivePackage = packageName
                    currentSessionStartTime = startTime
                }
            }
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
                    val insertedId = usageRepository.insertLog(
                        UsageLog(
                            packageName = packageName,
                            reason = reason,
                            timestamp = unlockTime,
                            durationMinutes = durationMin,
                        ),
                    )
                    // Seteamos el estado de la sesión activa
                    currentActiveLogId = insertedId
                    currentActivePackage = packageName
                    currentSessionStartTime = unlockTime
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
