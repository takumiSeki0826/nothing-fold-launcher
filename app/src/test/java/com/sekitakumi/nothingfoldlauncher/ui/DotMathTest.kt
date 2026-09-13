package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class DotMathTest {

    @Test
    fun `dotRadiusRatio maps zero luminance to minRatio`() {
        assertEquals(0.15f, dotRadiusRatio(0f, minRatio = 0.15f, maxRatio = 0.9f), 0.001f)
    }

    @Test
    fun `dotRadiusRatio maps full luminance to maxRatio`() {
        assertEquals(0.9f, dotRadiusRatio(1f, minRatio = 0.15f, maxRatio = 0.9f), 0.001f)
    }

    @Test
    fun `dotRadiusRatio maps mid luminance to midpoint`() {
        assertEquals(0.525f, dotRadiusRatio(0.5f, minRatio = 0.15f, maxRatio = 0.9f), 0.001f)
    }

    @Test
    fun `dotRadiusRatio clamps out-of-range luminance`() {
        assertEquals(0.15f, dotRadiusRatio(-1f, minRatio = 0.15f, maxRatio = 0.9f), 0.001f)
        assertEquals(0.9f, dotRadiusRatio(2f, minRatio = 0.15f, maxRatio = 0.9f), 0.001f)
    }

    @Test
    fun `litDotCount rounds ratio to nearest dot count`() {
        assertEquals(10, litDotCount(0.5f, totalDots = 20))
        assertEquals(0, litDotCount(0f, totalDots = 20))
        assertEquals(20, litDotCount(1f, totalDots = 20))
    }

    @Test
    fun `litDotCount clamps out-of-range ratio`() {
        assertEquals(0, litDotCount(-0.5f, totalDots = 20))
        assertEquals(20, litDotCount(1.5f, totalDots = 20))
    }

    @Test
    fun `dragPositionToRatio computes proportion of width`() {
        assertEquals(0.25f, dragPositionToRatio(25f, width = 100f), 0.001f)
    }

    @Test
    fun `dragPositionToRatio clamps outside bar bounds`() {
        assertEquals(0f, dragPositionToRatio(-10f, width = 100f), 0.001f)
        assertEquals(1f, dragPositionToRatio(150f, width = 100f), 0.001f)
    }

    @Test
    fun `dragPositionToRatio returns zero for non-positive width`() {
        assertEquals(0f, dragPositionToRatio(10f, width = 0f), 0.001f)
    }

    @Test
    fun `verticalDragToRatio maps top to one and bottom to zero`() {
        assertEquals(1f, verticalDragToRatio(0f, height = 100f), 0.001f)
        assertEquals(0f, verticalDragToRatio(100f, height = 100f), 0.001f)
        assertEquals(0.5f, verticalDragToRatio(50f, height = 100f), 0.001f)
    }

    @Test
    fun `verticalDragToRatio clamps outside bounds`() {
        assertEquals(1f, verticalDragToRatio(-10f, height = 100f), 0.001f)
        assertEquals(0f, verticalDragToRatio(150f, height = 100f), 0.001f)
    }
}
