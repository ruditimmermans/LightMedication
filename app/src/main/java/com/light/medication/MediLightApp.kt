package com.light.medication

import android.app.Application
import com.light.medication.util.AutoClearPreferences


class MediLightApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AutoClearPreferences.scheduleAutoClearWork(this)
    }
}
