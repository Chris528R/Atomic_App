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
import com.example.atomic.domain.repository.ScheduleRuleRepository
import com.example.atomic.domain.repository.UsageRepository
import com.example.atomic.overlay.WindowOverlayManager
import com.example.atomic.util.resolveAppDisplayName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import com.example.atomic.domain.TimeRuleEngine
import com.example.atomic.domain.UnlockReason
import com.example.atomic.data.myGoodHabits
import com.example.atomic.util.AppLauncher

class AppTrackerService : AccessibilityService() {

    private lateinit var overlayManager: WindowOverlayManager
    private lateinit var blockedAppRepository: BlockedAppRepository
    private lateinit var activePassRepository: ActivePassRepository
    private lateinit var usageRepository: UsageRepository
    private lateinit var scheduleRuleRepository: ScheduleRuleRepository
    private lateinit var database: AtomicDatabase

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    /** Paquete → instante de expiración del pase (epoch millis). */
    private val unlockedApps = mutableMapOf<String, Long>()

    @Volatile
    private var dynamicBlockedApps: List<String> = emptyList()

    @Volatile
    private var dynamicScheduleRules: List<com.example.atomic.data.ScheduleRule> = emptyList()

    // Estado de la sesión actual
    private var currentActiveLogId: Long? = null
    private var currentActivePackage: String? = null
    private var currentSessionStartTime: Long = 0L

    val positiveApps = listOf("com.sololearn", "com.getmimo")
    private var lastLoggedPositiveApp: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = WindowOverlayManager(applicationContext)
        database = AtomicDatabase.getDatabase(applicationContext)
        blockedAppRepository = BlockedAppRepositoryImpl(database.blockedAppDao())
        activePassRepository = ActivePassRepositoryImpl(database.activePassDao())
        usageRepository = UsageRepositoryImpl(database.usageLogDao())
        scheduleRuleRepository = com.example.atomic.data.repository.ScheduleRuleRepositoryImpl(database.scheduleRuleDao())

        serviceScope.launch {
            blockedAppRepository.getBlockedPackages().collect { packages ->
                dynamicBlockedApps = packages
            }
        }

        serviceScope.launch {
            scheduleRuleRepository.getAllRules().collect { rules ->
                dynamicScheduleRules = rules
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
        
        if (positiveApps.contains(packageName)) {
            if (packageName != lastLoggedPositiveApp) {
                lastLoggedPositiveApp = packageName
                
                serviceScope.launch {
                    usageRepository.insertLog(
                        UsageLog(
                            packageName = packageName,
                            reason = "Hábito Positivo",
                            timestamp = System.currentTimeMillis(),
                            durationMinutes = 0
                        )
                    )
                }
            }
            return
        }

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

        // 2. EVALUAR HORARIOS LIBRES
        if (TimeRuleEngine.isFreeTime(dynamicScheduleRules, LocalDateTime.now())) {
            val freeTimeDurationMin = UnlockReason.FREE_TIME.allowedMinutes
            val startTime = System.currentTimeMillis()
            
            // Conceder el pase silenciosamente
            unlockedApps[packageName] = startTime + (freeTimeDurationMin * 60 * 1000L)
            
            // Registrar en Room para no perder las estadísticas, pero sin lanzar la UI
            serviceScope.launch {
                activePassRepository.insertPass(com.example.atomic.data.ActivePass(packageName, unlockedApps[packageName]!!))
                val insertedId = usageRepository.insertLog(
                    UsageLog(
                        packageName = packageName,
                        reason = UnlockReason.FREE_TIME.title,
                        timestamp = startTime,
                        durationMinutes = freeTimeDurationMin
                    )
                )
                // Actualizar estado para medir uso real (Fase 1)
                currentActiveLogId = insertedId
                currentActivePackage = packageName
                currentSessionStartTime = startTime
            }
            return // Salimos del evento; la app se abre sin interrupciones
        }

        // 3. SI NO ES HORARIO LIBRE, MOSTRAR FRICCIÓN PROGRESIVA
        serviceScope.launch {
            val startOfDayMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val openCount = usageRepository.getTodayOpenCount(packageName, startOfDayMillis)

            database.timeDebtDao().initDebt()
            val debt = database.timeDebtDao().getDebt() ?: 0

            val dynamicReplacement = database.habitReplacementDao().getReplacementForApp(packageName)
            val suggestedHabit = dynamicReplacement?.let {
                com.example.atomic.data.PositiveHabit(
                    id = it.blockedPackageName,
                    name = it.replacementAppName,
                    targetPackageName = it.replacementPackageName
                )
            }

            withContext(Dispatchers.Main) {
                overlayManager.showFrictionScreen(
                    appName = resolveAppDisplayName(packageName),
                    openCount = openCount,
                    currentDebt = debt,
                    suggestedHabit = suggestedHabit,
                    onUnlock = { reasonEnum, isForced ->
                        serviceScope.launch {
                            var finalDurationMin = reasonEnum.allowedMinutes

                            if (isForced) {
                                finalDurationMin = 2
                                database.timeDebtDao().addDebt(15)
                            } else {
                                if (debt > 0) {
                                    val penaltyToApply = kotlin.math.min(debt, finalDurationMin - 1)
                                    finalDurationMin -= penaltyToApply
                                    database.timeDebtDao().reduceDebt(penaltyToApply)
                                }
                            }

                            val unlockTime = System.currentTimeMillis()
                            val passExpiry = unlockTime + finalDurationMin * 60 * 1000L
                            unlockedApps[packageName] = passExpiry

                            activePassRepository.insertPass(com.example.atomic.data.ActivePass(packageName, passExpiry))
                            val insertedId = usageRepository.insertLog(
                                UsageLog(
                                    packageName = packageName,
                                    reason = reasonEnum.title,
                                    timestamp = unlockTime,
                                    durationMinutes = finalDurationMin,
                                ),
                            )
                            currentActiveLogId = insertedId
                            currentActivePackage = packageName
                            currentSessionStartTime = unlockTime
                        }
                    },
                    onRedirect = { targetPackage ->
                        AppLauncher.launchApp(this@AppTrackerService, targetPackage)
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
        }
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
