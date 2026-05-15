package com.light.medication.util

import android.content.Context
import java.util.Calendar

object TimeUtils {
    fun formatTime(hour: Int, minute: Int): String {
        return String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute)
    }
}
