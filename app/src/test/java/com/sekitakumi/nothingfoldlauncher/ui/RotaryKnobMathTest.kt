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

    @Test
    fun `offset straight up maps to zero degrees`() {
        assertEquals(0f, angleDegFromCenterOffset(dx = 0f, dy = -10f), 0.001f)
    }

    @Test
    fun `offset to the right maps to 90 degrees`() {
        assertEquals(90f, angleDegFromCenterOffset(dx = 10f, dy = 0f), 0.001f)
    }

    @Test
    fun `offset straight down maps to 180 degrees`() {
        assertEquals(180f, angleDegFromCenterOffset(dx = 0f, dy = 10f), 0.001f)
    }

    @Test
    fun `offset to the left maps to 270 degrees`() {
        assertEquals(270f, angleDegFromCenterOffset(dx = -10f, dy = 0f), 0.001f)
    }

    @Test
    fun `angleDegFromCenterOffset is the inverse of angleToIndicatorOffset`() {
        val original = 137f
        val offset = angleToIndicatorOffset(angleDeg = original, radiusPx = 25f)
        assertEquals(original, angleDegFromCenterOffset(dx = offset.x, dy = offset.y), 0.01f)
    }

    @Test
    fun `jog wheel arc start angle for a wheel angle of zero`() {
        assertEquals(-110f, jogWheelArcStartAngleDeg(centerAngleDeg = 0f, arcWidthDeg = 40f), 0.001f)
    }

    @Test
    fun `jog wheel arc start angle for a wheel angle of 90`() {
        assertEquals(-10f, jogWheelArcStartAngleDeg(centerAngleDeg = 90f, arcWidthDeg = 20f), 0.001f)
    }

    @Test
    fun `touch at the exact center hits the jog wheel spindle`() {
        assertEquals(true, isJogWheelCenterHit(dx = 0f, dy = 0f, discRadiusPx = 100f))
    }

    @Test
    fun `touch inside the spindle hit radius counts as center`() {
        // 0.24 of the disc radius = 24px
        assertEquals(true, isJogWheelCenterHit(dx = 16f, dy = 16f, discRadiusPx = 100f))
    }

    @Test
    fun `touch outside the spindle hit radius is not center`() {
        assertEquals(false, isJogWheelCenterHit(dx = 25f, dy = 0f, discRadiusPx = 100f))
        assertEquals(false, isJogWheelCenterHit(dx = 0f, dy = -60f, discRadiusPx = 100f))
    }
}
