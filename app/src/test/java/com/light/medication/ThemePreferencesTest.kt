package com.light.medication

import com.light.medication.util.ThemePreferences
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemePreferencesTest {

    @Test
    fun testThemeConstants() {
        assertEquals("DARK", ThemePreferences.THEME_DARK)
        assertEquals("EINK", ThemePreferences.THEME_EINK)
        assertEquals("LIGHT", ThemePreferences.THEME_LIGHT)
        assertEquals("SYSTEM", ThemePreferences.THEME_SYSTEM)
    }
}
