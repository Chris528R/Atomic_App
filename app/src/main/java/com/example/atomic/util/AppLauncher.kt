package com.example.atomic.util

import android.content.Context
import android.content.Intent
import android.widget.Toast

object AppLauncher {
    fun launchApp(context: Context, packageName: String) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) 
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) 
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "App no instalada", Toast.LENGTH_SHORT).show()
        }
    }
}
