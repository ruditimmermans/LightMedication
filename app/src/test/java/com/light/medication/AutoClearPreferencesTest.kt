package com.light.medication

import com.light.medication.util.AutoClearPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class AutoClearPreferencesTest {

    private val fixedNow = ZonedDateTime.of(2026, 9, 26, 12, 0, 0, 0, ZoneId.of("UTC"))

    @Test
    fun testCutoffTimestamp_Off() {
        val cutoff = AutoClearPreferences.getCutoffTimestamp(AutoClearPreferences.INTERVAL_OFF, fixedNow)
        assertNull(cutoff)
    }

    @Test
    fun testCutoffTimestamp_Weekly() {
        val cutoff = AutoClearPreferences.getCutoffTimestamp(AutoClearPreferences.INTERVAL_WEEKLY, fixedNow)
        assertNotNull(cutoff)
        val expected = fixedNow.minusWeeks(1).toInstant().toEpochMilli()
        assertEquals(expected, cutoff)
    }

    @Test
    fun testCutoffTimestamp_Monthly() {
        val cutoff = AutoClearPreferences.getCutoffTimestamp(AutoClearPreferences.INTERVAL_MONTHLY, fixedNow)
        assertNotNull(cutoff)
        val expected = fixedNow.minusMonths(1).toInstant().toEpochMilli()
        assertEquals(expected, cutoff)
    }

    @Test
    fun testCutoffTimestamp_ThreeMonths() {
        val cutoff = AutoClearPreferences.getCutoffTimestamp(AutoClearPreferences.INTERVAL_THREE_MONTHS, fixedNow)
        assertNotNull(cutoff)
        val expected = fixedNow.minusMonths(3).toInstant().toEpochMilli()
        assertEquals(expected, cutoff)
    }

    @Test
    fun testCutoffTimestamp_FiveMonths() {
        val cutoff = AutoClearPreferences.getCutoffTimestamp(AutoClearPreferences.INTERVAL_FIVE_MONTHS, fixedNow)
        assertNotNull(cutoff)
        val expected = fixedNow.minusMonths(5).toInstant().toEpochMilli()
        assertEquals(expected, cutoff)
    }

    @Test
    fun testCutoffTimestamp_Yearly() {
        val cutoff = AutoClearPreferences.getCutoffTimestamp(AutoClearPreferences.INTERVAL_YEARLY, fixedNow)
        assertNotNull(cutoff)
        val expected = fixedNow.minusYears(1).toInstant().toEpochMilli()
        assertEquals(expected, cutoff)
    }
}
