package com.light.medication

import com.light.medication.util.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class TimeUtilsTest {

    @Test
    fun testFormatTime() {
        assertEquals("08:05", TimeUtils.formatTime(8, 5))
        assertEquals("20:30", TimeUtils.formatTime(20, 30))
        assertEquals("00:00", TimeUtils.formatTime(0, 0))
    }

    @Test
    fun testIsTakenToday_Null() {
        assertFalse(TimeUtils.isTakenToday(null))
    }

    @Test
    fun testIsTakenToday_Today() {
        val nowMillis = System.currentTimeMillis()
        assertTrue(TimeUtils.isTakenToday(nowMillis))
    }

    @Test
    fun testIsTakenToday_Yesterday() {
        val yesterdayMillis = LocalDateTime.now(ZoneId.systemDefault())
            .minusDays(1)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        assertFalse(TimeUtils.isTakenToday(yesterdayMillis))
    }
}
