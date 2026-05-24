# Atomic

**Atomic** es una aplicación Android para el control de hábitos digitales. Intercepta la apertura de apps seleccionadas, muestra una pantalla de fricción que obliga al usuario a elegir un motivo antes de continuar, concede ventanas temporales de acceso (15 min) y registra cada desbloqueo en una base de datos local para analizar patrones de uso.

---

## Objetivo del proyecto

Ayudar al usuario a tomar conciencia y control sobre el tiempo que pasa en apps concretas, combinando:

- **Intercepción a nivel de sistema** (`AccessibilityService`) para detectar qué app está en primer plano.
- **Overlay con Compose** (`SYSTEM_ALERT_WINDOW`) para la pantalla de fricción sobre otras apps.
- **Persistencia local** (Room) para historial de motivos y accesos.
- **UI principal** (Compose + ViewModel) para permisos y estadísticas en tiempo real.

---

## Stack tecnológico

| Capa | Tecnología | Versión (catálogo) |
|------|------------|-------------------|
| Lenguaje | Kotlin | 2.3.20 |
| UI | Jetpack Compose + Material 3 | BOM `2026.05.00` |
| Arquitectura UI | ViewModel + `StateFlow` + Lifecycle Compose | 2.9.3 |
| Concurrencia | Kotlin Coroutines | 1.10.2 |
| Persistencia | Room (KSP) + `Flow` reactivo | 2.8.4 |
| Procesamiento de anotaciones | KSP | 2.3.6 |
| Build | AGP + Gradle | 9.1.1 / 9.3.1 |
| Compilador Compose | Plugin `kotlin-compose` | (alineado con Kotlin 2.3.20) |

### Requisitos de entorno

- **JDK 17** (requerido por Android Gradle Plugin 9.x)
- **Android Studio** reciente con soporte para `compileSdk` 36
- Dispositivo o emulador con **API 24+** (`minSdk`)

### Notas de build

- **AGP 9** incluye Kotlin integrado (*built-in Kotlin*): no se aplica el plugin `org.jetbrains.kotlin.android`.
- **Room** usa `ksp()` en lugar de `kapt`.
- Las versiones de Compose se centralizan con el **Compose BOM** en `gradle/libs.versions.toml`.
- **KSP ≥ 2.3.1** es necesario para compatibilidad con built-in Kotlin de AGP 9.

---

## Arquitectura

```
┌──────────────────────────────────────────────────────────────────┐
│                    MainActivity (launcher)                        │
│  AtomicApp ─ NavigationBar: [ Apps | Estadísticas ]                │
│    · OnboardingScreen + PermissionsViewModel                     │
│    · BlockedAppsScreen + BlockedAppsViewModel ← Flow ← Room      │
│    · StatsScreen (UsageBarChart/DonutChart) + UsageViewModel     │
└────────────────────────────┬─────────────────────────────────────┘
                             │ lectura (Flow)
┌────────────────────────────▼─────────────────────────────────────┐
│                         Capa data/                                │
│  AtomicDatabase · UsageLog · BlockedApp · DAOs (Flow)              │
└────────────────────────────▲─────────────────────────────────────┘
                             │ insert (IO + Coroutines)
┌────────────────────────────┴─────────────────────────────────────┐
│                    Capa intercepción (sistema)                    │
│  AppTrackerService (AccessibilityService)                         │
│    · TYPE_WINDOW_STATE_CHANGED                                    │
│    · dynamicBlockedApps ← Flow (lista configurable)               │
│    · unlockedApps (pases en memoria, 15 min)                      │
│    · WindowOverlayManager → FrictionScreen (Compose overlay)      │
└──────────────────────────────────────────────────────────────────┘
```

### Componentes

| Componente | Ubicación | Responsabilidad |
|------------|-----------|-----------------|
| `MainActivity` | `MainActivity.kt` | Punto de entrada; hospeda `AtomicApp` y ambos ViewModels. |
| `AtomicApp` | `ui/AtomicApp.kt` | Navegación inferior entre Apps y Estadísticas. |
| `OnboardingScreen` | `ui/OnboardingScreen.kt` | Onboarding guiado de permisos (overlay, accesibilidad, batería). |
| `PermissionsViewModel` | `ui/PermissionsViewModel.kt` | Estado de permisos; refresco en `onResume`. |
| `StatsScreen` | `ui/StatsScreen.kt` | Resumen visual (UsageBarChart/MotivesDonutChart) e historial. |
| `UsageViewModel` | `ui/UsageViewModel.kt` | Observa `Flow<List<UsageLog>>` y expone `StateFlow`. |
| `FrictionScreen` | `ui/FrictionScreen.kt` | UI de fricción (motivo obligatorio + botón Abrir). |
| `WindowOverlayManager` | `overlay/WindowOverlayManager.kt` | Inyecta Compose en `TYPE_APPLICATION_OVERLAY`. |
| `AppTrackerService` | `service/AppTrackerService.kt` | Detecta apps, gestiona pases y persiste logs. |
| `AtomicDatabase` | `data/AtomicDatabase.kt` | Singleton Room (`atomic_database`). |
| `UsageLog` / `UsageLogDao` | `data/` | Entidad, inserción y consulta reactiva de logs. |
| `BlockedApp` / `BlockedAppDao` | `data/` | Apps bloqueadas configurables (`Flow` de packages). |
| `BlockedAppsScreen` | `ui/BlockedAppsScreen.kt` | Lista de apps instaladas con switch on/off. |
| `BlockedAppsViewModel` | `ui/BlockedAppsViewModel.kt` | PackageManager + Room; búsqueda y toggles. |
| `getInstalledApps` | `util/InstalledApps.kt` | Apps de usuario (excluye sistema y Atomic). |
| `PermissionChecker` | `util/PermissionChecker.kt` | Comprueba y abre pantallas de Ajustes. |
| `resolveAppDisplayName` | `util/AppDisplayNames.kt` | Mapeo package → nombre legible. |

---

## Flujo de datos

### 1. Detección y fricción

1. El usuario activa **Atomic** en Accesibilidad y concede **Mostrar sobre otras apps**.
2. Al abrir una app bloqueada (Instagram, Facebook, YouTube), `AppTrackerService` recibe `TYPE_WINDOW_STATE_CHANGED`.
3. Si no hay **pase activo** en `unlockedApps`, `WindowOverlayManager` muestra `FrictionScreen`.
4. **Cancelar** → `Intent` al launcher (Home).
5. **Abrir (15 min)** → se guarda expiración en memoria y se inserta un `UsageLog` en Room vía `serviceScope` (`Dispatchers.IO`).

### 2. Pases temporales

```text
unlockedApps[packageName] = unlockTime + (15 * 60 * 1000)
```

Mientras `System.currentTimeMillis() < expiry`, la app bloqueada abre sin overlay. Los pases se guardan ahora en Room (tabla `active_passes`) mediante un flujo reactivo, por lo que sobreviven a reinicios del servicio. Al expirar, se eliminan tanto de memoria como de la base de datos.

### 3. Estadísticas en tiempo real

```text
Room INSERT → Flow emite nueva lista → UsageViewModel → StatsScreen (Compose)
```

El DAO expone `getAllLogs(): Flow<List<UsageLog>>`; no hace falta refrescar manualmente al volver a la pestaña Estadísticas.

### Apps bloqueadas (configurables)

Tabla Room: `blocked_apps` (`packageName`, `appName`).

- El servicio se suscribe a `getBlockedPackages(): Flow<List<String>>` en `onServiceConnected` y actualiza `dynamicBlockedApps` en memoria (sin consultar la DB en cada evento).
- La pestaña **Apps** lista apps instaladas (no sistema) vía `PackageManager`; un switch añade o quita entradas.
- En la primera ejecución se insertan por defecto: Instagram, Facebook y YouTube (`DefaultBlockedApps`).
- **DB v2** + `fallbackToDestructiveMigration`: al actualizar desde v1, reinstala o deja que Room recree tablas (MVP).

---

## Modelo de datos (`UsageLog`)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | `Int` | PK autogenerada |
| `packageName` | `String` | App desbloqueada |
| `reason` | `String` | Motivo elegido en `FrictionScreen` |
| `timestamp` | `Long` | Epoch millis del desbloqueo |
| `durationMinutes` | `Int` | Duración del pase (15) |

Tabla Room: `usage_logs`

---

## Permisos críticos

### `AccessibilityService` (`AppTrackerService`)

**Qué es:** API que permite recibir eventos del sistema sobre cambios en la interfaz.

**Por qué lo usa Atomic:** Detectar **qué aplicación está en primer plano** de forma inmediata. Solo escucha:

```xml
android:accessibilityEventTypes="typeWindowStateChanged"
```

**Activación:** Ajustes → Accesibilidad → Servicios instalados → **Atomic**.

**Flags** (`accessibility_service_config.xml`):

| Flag / atributo | Propósito |
|-----------------|-----------|
| `flagDefault` | Comportamiento base del servicio. |
| `flagRetrieveInteractiveWindows` | Ventanas interactivas en segundo plano. |
| `flagReportViewIds` | IDs de vistas en la UI activa. |
| `canRetrieveWindowContent="true"` | Metadatos de ventana (`packageName`). |

```xml
<service
    android:name=".service.AppTrackerService"
    android:exported="true"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
    android:stopWithTask="false" />
```

---

### `SYSTEM_ALERT_WINDOW` (Mostrar sobre otras apps)

**Qué es:** Permite dibujar vistas encima de otras apps (`TYPE_APPLICATION_OVERLAY`).

**Por qué lo usa Atomic:** Mostrar `FrictionScreen` sin salir de la app que el usuario intenta abrir.

**Activación:** Ajustes → Apps → Atomic → **Mostrar sobre otras apps**.

> **Privacidad:** Estos permisos son sensibles y necesarios para el núcleo de la aplicación. La pestaña **Permisos** de la app explica su uso antes de solicitarlos.

---

### `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` (Batería sin restricciones)

**Qué es:** Permite pedirle al usuario que excluya la app de las optimizaciones de batería del sistema.

**Por qué lo usa Atomic:** Android suele matar los servicios en segundo plano (`AppTrackerService`) para ahorrar batería. Sin este permiso, el interceptor dejaría de funcionar tras un tiempo inactivo.

**Activación:** Ajustes → Aplicaciones → Atomic → Batería → **Sin restricciones** (o mediante el intent en la pestaña Permisos).

---

## Árbol de carpetas

Estructura del código fuente (sin `build/`, `.gradle/` ni artefactos generados):

```
Atomic/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/atomic/
│       │   │   ├── MainActivity.kt
│       │   │   ├── data/
│       │   │   │   ├── repository/
│       │   │   │   │   ├── ActivePassRepositoryImpl.kt
│       │   │   │   │   ├── BlockedAppRepositoryImpl.kt
│       │   │   │   │   ├── ScheduleRuleRepositoryImpl.kt
│       │   │   │   │   └── UsageRepositoryImpl.kt
│       │   │   │   ├── ActivePass.kt
│       │   │   │   ├── ActivePassDao.kt
│       │   │   │   ├── AtomicDatabase.kt
│       │   │   │   ├── BlockedApp.kt
│       │   │   │   ├── BlockedAppDao.kt
│       │   │   │   ├── DefaultBlockedApps.kt
│       │   │   │   ├── ScheduleRule.kt
│       │   │   │   ├── ScheduleRuleDao.kt
│       │   │   │   ├── TimeDebt.kt
│       │   │   │   ├── TimeDebtDao.kt
│       │   │   │   ├── UsageLog.kt
│       │   │   │   └── UsageLogDao.kt
│       │   │   ├── domain/
│       │   │   │   ├── repository/
│       │   │   │   │   ├── ActivePassRepository.kt
│       │   │   │   │   ├── BlockedAppRepository.kt
│       │   │   │   │   ├── ScheduleRuleRepository.kt
│       │   │   │   │   └── UsageRepository.kt
│       │   │   │   ├── TimeRuleEngine.kt
│       │   │   │   └── UnlockReason.kt
│       │   │   ├── overlay/
│       │   │   │   └── WindowOverlayManager.kt
│       │   │   ├── service/
│       │   │   │   └── AppTrackerService.kt
│       │   │   ├── ui/
│       │   │   │   ├── AtomicApp.kt
│       │   │   │   ├── BlockedAppsScreen.kt
│       │   │   │   ├── BlockedAppsViewModel.kt
│       │   │   │   ├── BlockedAppsViewModelFactory.kt
│       │   │   │   ├── FrictionScreen.kt
│       │   │   │   ├── MotivesDonutChart.kt
│       │   │   │   ├── OnboardingScreen.kt
│       │   │   │   ├── PermissionsViewModel.kt
│       │   │   │   ├── ScheduleSettingsScreen.kt
│       │   │   │   ├── ScheduleSettingsViewModel.kt
│       │   │   │   ├── StatsScreen.kt
│       │   │   │   ├── UsageBarChart.kt
│       │   │   │   ├── UsageViewModel.kt
│       │   │   │   ├── UsageViewModelFactory.kt
│       │   │   │   └── theme/
│       │   │   │       └── AtomicTheme.kt
│       │   │   └── util/
│       │   │       ├── AppDisplayNames.kt
│       │   │       ├── InstalledApps.kt
│       │   │       └── PermissionChecker.kt
│       │   └── res/
│       │       ├── drawable/
│       │       ├── mipmap-*/
│       │       ├── values/
│       │       │   ├── colors.xml
│       │       │   ├── strings.xml
│       │       │   └── themes.xml
│       │       ├── values-night/
│       │       │   └── themes.xml
│       │       └── xml/
│       │           ├── accessibility_service_config.xml
│       │           ├── backup_rules.xml
│       │           └── data_extraction_rules.xml
│       ├── test/java/com/example/atomic/
│       │   └── ui/
│       │       └── UsageViewModelTest.kt
│       └── androidTest/java/com/example/atomic/
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── README.md
```

### Convención de paquetes (`com.example.atomic`)

| Paquete | Contenido |
|---------|-----------|
| `data/` | Room: entidades, DAO, base de datos. Implementaciones de repositorios. |
| `domain/` | Contratos (interfaces) de repositorios y reglas de negocio. |
| `service/` | Servicios de sistema (`AppTrackerService`). |
| `overlay/` | Gestión de ventanas flotantes con Compose. |
| `ui/` | Pantallas Compose, ViewModels, tema, navegación. |
| `util/` | Helpers (permisos, nombres de apps). |

---

## Configuración en el dispositivo

1. Instalar la app:

```bash
./gradlew :app:installDebug
```

2. Abrir **Atomic** → Completar el **Onboarding guiado** para activar **Mostrar sobre otras apps**, **Accesibilidad** y **Batería sin restricciones**.
3. Probar abriendo Instagram / YouTube / Facebook → debe aparecer la pantalla de fricción.
4. Pestaña **Apps** → activar o desactivar apps bloqueadas.
5. Elegir un motivo → **Abrir (15 min)** → revisar la pestaña **Estadísticas**.

### Inspeccionar la base de datos

Android Studio → **App Inspection** → **Database Inspector** → `atomic_database` → tabla `usage_logs`.

---

## Estado actual

| Área | Estado |
|------|--------|
| Gradle + Compose + Room + Coroutines | ✅ Configurado |
| `AppTrackerService` + accesibilidad | ✅ Implementado |
| Overlay + `FrictionScreen` | ✅ Implementado |
| Pases de 15 min (`unlockedApps`) | ✅ Implementado |
| Persistencia Room + `Flow` reactivo | ✅ Implementado |
| `MainActivity` + onboarding permisos | ✅ Implementado |
| `StatsScreen` + `UsageViewModel` | ✅ Implementado |
| Lista de apps bloqueadas configurable | ✅ Implementado |
| Persistir pases en Room (sobrevivir reinio) | ✅ Implementado |
| Capa `domain/` + repositorios | ✅ Implementado |
| Tests unitarios / UI | ✅ Implementado |

---

## Roadmap

### Fase 1 — Refinar el MVP

| Área | Estado |
|------|--------|
| Registrar duración real de uso | ✅ Implementado |
| Historial y estadísticas visuales | ✅ Implementado |
| Mejorar onboarding guiado | ✅ Implementado |

### Fase 2 — Reglas inteligentes

| Área | Estado |
|------|--------|
| Reglas por motivo (`UnlockReason`) | ✅ Implementado |
| Penalización futura (deuda de tiempo) | ✅ Implementado |
| Fricción progresiva | ✅ Implementado |
| Horarios libres (Reglas de tiempo) | ✅ Implementado |

### Fase 3 — Aprendizaje y patrones

| Área | Estado |
|------|--------|
| Detección de patrones de uso | ⏳ Planeado |
| Insights automáticos | ⏳ Planeado |
| Metas y objetivos semanales | ⏳ Planeado |

### Fase 4 — Automatización de buenos hábitos

| Área | Estado |
|------|--------|
| Recordatorios inteligentes | ⏳ Planeado |
| Recomendaciones de hábitos | ⏳ Planeado |
| Sistema de reemplazo de hábitos | ⏳ Planeado |

### Fase 5 — Plataforma

| Área | Estado |
|------|--------|
| Sincronización entre dispositivos | ⏳ Planeado |
| Exportación de datos (CSV / JSON) | ⏳ Planeado |
| Backup en nube / cuentas | ⏳ Planeado |

### Ideas experimentales

| Área | Estado |
|------|--------|
| Modo desafío | 💡 Idea |
| Modo compañero | 💡 Idea |
| Reemplazo contextual ("Estoy aburrido" → alternativas) | 💡 Idea |

---

## Referencias

- [Jetpack Compose BOM](https://developer.android.com/develop/ui/compose/bom)
- [Room con KSP](https://developer.android.com/jetpack/androidx/releases/room)
- [Room + Flow](https://developer.android.com/kotlin/flow)
- [Crear un servicio de accesibilidad](https://developer.android.com/guide/topics/ui/accessibility/service)
- [Migración a built-in Kotlin (AGP 9)](https://developer.android.com/build/migrate-to-built-in-kotlin)
- [Permiso SYSTEM_ALERT_WINDOW](https://developer.android.com/reference/android/Manifest.permission#SYSTEM_ALERT_WINDOW)

---

*Documentación del proyecto Atomic — control de hábitos digitales en Android.*
