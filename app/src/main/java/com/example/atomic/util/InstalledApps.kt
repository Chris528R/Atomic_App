package com.example.atomic.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

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
        .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
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
