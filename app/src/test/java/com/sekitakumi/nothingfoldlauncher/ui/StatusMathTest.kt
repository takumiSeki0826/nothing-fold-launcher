package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class StatusMathTest {

    @Test
    fun `batteryPercent computes percentage from level and scale`() {
        assertEquals(50, batteryPercent(level = 50, scale = 100))
        assertEquals(100, batteryPercent(level = 100, scale = 100))
        assertEquals(0, batteryPercent(level = 0, scale = 100))
    }

    @Test
    fun `batteryPercent returns zero for non-positive scale`() {
        assertEquals(0, batteryPercent(level = 50, scale = 0))
        assertEquals(0, batteryPercent(level = 50, scale = -1))
    }

    @Test
    fun `batteryPercent clamps to 0 to 100`() {
        assertEquals(100, batteryPercent(level = 150, scale = 100))
        assertEquals(0, batteryPercent(level = -10, scale = 100))
    }

    @Test
    fun `signalBars clamps to 0 to 4`() {
        assertEquals(0, signalBars(-1))
        assertEquals(0, signalBars(0))
        assertEquals(4, signalBars(4))
        assertEquals(4, signalBars(10))
    }
}
