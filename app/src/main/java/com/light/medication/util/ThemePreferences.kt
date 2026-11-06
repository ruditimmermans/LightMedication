package com.light.medication.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object ThemePreferences {
    private const val PREFS_NAME = "medilight_prefs"
    private const val KEY_THEME = "app_theme"

    const val THEME_DARK = "DARK"
    const val THEME_EINK = "EINK"
    const val THEME_LIGHT = "LIGHT"
    const val THEME_SYSTEM = "SYSTEM"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getTheme(context: Context): String {
        return getSharedPreferences(context).getString(KEY_THEME, THEME_DARK) ?: THEME_DARK
    }

    fun setTheme(context: Context, theme: String) {
        getSharedPreferences(context).edit {
            putString(KEY_THEME, theme)
        }
    }
}
