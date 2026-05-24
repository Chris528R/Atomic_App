package com.example.atomic.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.atomic.data.DefaultBlockedApps

data class InstalledAppInfo(
    val appName: String,
    val packageName: String,
    val isBlocked: Boolean,
)

fun getInstalledApps(context: Context, blockedPackages: List<String>): List<InstalledAppInfo> {
    val pm = context.packageManager
    val ownPackage = context.packageName

    return pm.getInstalledApplications(PackageManager.GET_META_DATA)
        .asSequence()
        .filter { appInfo ->
            val isUserApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
            val isDefaultBlocked = DefaultBlockedApps.seed.any { it.packageName == appInfo.packageName }
            isUserApp || isDefaultBlocked
        }
        .filter { it.packageName != ownPackage }
        .map { appInfo ->
            InstalledAppInfo(
                appName = pm.getApplicationLabel(appInfo).toString(),
                packageName = appInfo.packageName,
                isBlocked = blockedPackages.contains(appInfo.packageName),
            )
        }
        .sortedBy { it.appName.lowercase() }
        .toList()
}
