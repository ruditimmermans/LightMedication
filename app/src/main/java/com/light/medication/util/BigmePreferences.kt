package com.light.medication.util

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.edit
import com.light.medication.BigmeKeepAliveService

object BigmePreferences {
    private const val PREFS_NAME = "medilight_prefs"
    private const val KEY_BIGME_MODE = "bigme_mode_enabled"

    fun isBigmeModeEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_BIGME_MODE, false)
    }

    fun setBigmeModeEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putBoolean(KEY_BIGME_MODE, enabled)
        }
        if (enabled) {
            startKeepAliveService(context)
        } else {
            stopKeepAliveService(context)
        }
    }

    fun startKeepAliveService(context: Context) {
        if (!isBigmeModeEnabled(context)) return
        val serviceIntent = Intent(context, BigmeKeepAliveService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopKeepAliveService(context: Context) {
        val serviceIntent = Intent(context, BigmeKeepAliveService::class.java)
        try {
            context.stopService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
