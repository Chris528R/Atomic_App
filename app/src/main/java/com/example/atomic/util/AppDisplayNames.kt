package com.example.atomic.util

fun resolveAppDisplayName(packageName: String): String = when (packageName) {
    "com.instagram.android" -> "Instagram"
    "com.facebook.katana" -> "Facebook"
    "com.google.android.youtube" -> "YouTube"
    else -> packageName
        .substringAfterLast('.')
        .replaceFirstChar { char -> char.uppercaseChar() }
        .ifEmpty { packageName }
}
