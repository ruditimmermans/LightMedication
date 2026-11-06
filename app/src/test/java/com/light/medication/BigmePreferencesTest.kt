package com.light.medication

import com.light.medication.util.BigmePreferences
import org.junit.Assert.assertNotNull
import org.junit.Test

class BigmePreferencesTest {

    @Test
    fun testBigmePreferencesExists() {
        assertNotNull(BigmePreferences)
    }
}
