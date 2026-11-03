package com.light.medication.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.light.medication.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object AutoClearPreferences {
    private const val PREFS_NAME = "medilight_prefs"
    private const val KEY_AUTO_CLEAR_INTERVAL = "auto_clear_interval"
    private const val WORK_NAME = "medilight_log_cleanup_work"

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
        scheduleAutoClearWork(context)
    }

    fun scheduleAutoClearWork(context: Context) {
        val interval = getAutoClearInterval(context)
        val workManager = WorkManager.getInstance(context)
        if (interval == INTERVAL_OFF) {
            workManager.cancelUniqueWork(WORK_NAME)
        } else {
            val cleanupRequest = PeriodicWorkRequestBuilder<LogCleanupWorker>(
                1, TimeUnit.DAYS,
            ).build()
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                cleanupRequest,
            )
        }
    }

    fun getCutoffTimestamp(interval: String, now: ZonedDateTime = ZonedDateTime.now()): Long? {
        val cutoff = when (interval) {
            INTERVAL_WEEKLY, "OLDER_WEEK", "SINCE_WEEK" -> now.minusWeeks(1)
            INTERVAL_MONTHLY, "OLDER_MONTH", "SINCE_MONTH" -> now.minusMonths(1)
            INTERVAL_THREE_MONTHS, "OLDER_THREE_MONTHS", "SINCE_THREE_MONTHS" -> now.minusMonths(3)
            INTERVAL_FIVE_MONTHS, "OLDER_FIVE_MONTHS", "SINCE_FIVE_MONTHS" -> now.minusMonths(5)
            INTERVAL_YEARLY, "OLDER_YEAR", "SINCE_YEAR" -> now.minusYears(1)
            else -> null
        }
        return cutoff?.toInstant()?.toEpochMilli()
    }

    suspend fun performAutoClear(context: Context, explicitInterval: String? = null) = withContext(Dispatchers.IO) {
        val interval = explicitInterval ?: getAutoClearInterval(context)
        val cutoff = getCutoffTimestamp(interval) ?: return@withContext
        val db = AppDatabase.getDatabase(context)
        db.medicationLogDao().deleteOlderThan(cutoff)
    }
}
