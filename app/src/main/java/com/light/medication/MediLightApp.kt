package com.light.medication

import android.app.Application
import com.light.medication.util.AutoClearPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediLightApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AutoClearPreferences.scheduleAutoClearWork(this)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AutoClearPreferences.performAutoClear(this@MediLightApp)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
