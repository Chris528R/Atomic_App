package com.example.atomic.data

object DefaultBlockedApps {
    val seed: List<BlockedApp> = listOf(
        BlockedApp(packageName = "com.instagram.android", appName = "Instagram"),
        BlockedApp(packageName = "com.facebook.katana", appName = "Facebook"),
        BlockedApp(packageName = "com.google.android.youtube", appName = "YouTube"),
    )
}
