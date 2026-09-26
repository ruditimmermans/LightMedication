package com.light.medication.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.light.medication.data.AppDatabase
import java.time.ZonedDateTime

object AutoClearPreferences {
    private const val PREFS_NAME = "medilight_prefs"
    private const val KEY_AUTO_CLEAR_INTERVAL = "auto_clear_interval"

    const val INTERVAL_OFF = "OFF"
    const val INTERVAL_WEEKLY = "WEEKLY"
    const val INTERVAL_MONTHLY = "MONTHLY"
    const val INTERVAL_THREE_MONTHS = "THREE_MONTHS"
    const val INTERVAL_FIVE_MONTHS = "FIVE_MONTHS"
    const val INTERVAL_YEARLY = "YEARLY"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getAutoClearInterval(context: Context): String {
        return getSharedPreferences(context).getString(KEY_AUTO_CLEAR_INTERVAL, INTERVAL_OFF) ?: INTERVAL_OFF
    }

    fun setAutoClearInterval(context: Context, interval: String) {
        getSharedPreferences(context).edit {
            putString(KEY_AUTO_CLEAR_INTERVAL, interval)
        }
    }

    fun getCutoffTimestamp(interval: String, now: ZonedDateTime = ZonedDateTime.now()): Long? {
        val cutoff = when (interval) {
            INTERVAL_WEEKLY -> now.minusWeeks(1)
            INTERVAL_MONTHLY -> now.minusMonths(1)
            INTERVAL_THREE_MONTHS -> now.minusMonths(3)
            INTERVAL_FIVE_MONTHS -> now.minusMonths(5)
            INTERVAL_YEARLY -> now.minusYears(1)
            else -> null
        }
        return cutoff?.toInstant()?.toEpochMilli()
    }

    suspend fun performAutoClear(context: Context) {
        val interval = getAutoClearInterval(context)
        val cutoff = getCutoffTimestamp(interval) ?: return
        val db = AppDatabase.getDatabase(context)
        db.medicationLogDao().deleteOlderThan(cutoff)
    }
}
