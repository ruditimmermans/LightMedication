package com.light.medication.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class LogCleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            AutoClearPreferences.performAutoClear(applicationContext)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
