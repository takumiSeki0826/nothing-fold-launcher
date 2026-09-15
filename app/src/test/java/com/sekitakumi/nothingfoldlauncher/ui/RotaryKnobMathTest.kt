package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

class RotaryKnobMathTest {

    @Test
    fun `zero degrees points straight up`() {
        val offset = angleToIndicatorOffset(angleDeg = 0f, radiusPx = 10f)
        assertEquals(0f, offset.x, 0.001f)
        assertEquals(-10f, offset.y, 0.001f)
    }

    @Test
    fun `positive 90 degrees points right`() {
        val offset = angleToIndicatorOffset(angleDeg = 90f, radiusPx = 10f)
        assertEquals(10f, offset.x, 0.001f)
        assertEquals(0f, offset.y, 0.001f)
    }

    @Test
    fun `negative 90 degrees points left`() {
        val offset = angleToIndicatorOffset(angleDeg = -90f, radiusPx = 10f)
        assertEquals(-10f, offset.x, 0.001f)
        assertEquals(0f, offset.y, 0.001f)
    }

    @Test
    fun `first knob in order has no entrance wobble delay`() {
        assertEquals(0L, entranceWobbleDelayMs(orderIndex = 0))
    }

    @Test
    fun `later knobs are delayed by a multiple of the stagger step`() {
        assertEquals(40L, entranceWobbleDelayMs(orderIndex = 1))
        assertEquals(200L, entranceWobbleDelayMs(orderIndex = 5))
    }
}
