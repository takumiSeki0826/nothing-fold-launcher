package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarDotGridTest {

    @Test
    fun `lays out September 2026 with a 2-cell leading offset and today marked`() {
        // September 1 2026 is a Tuesday, so the grid's Sunday-first offset is 2.
        val grid = calendarDotGrid(year = 2026, month = 9, today = 14)

        assertEquals(7, grid.columns)
        assertEquals(30, grid.dots.size)

        val day1 = grid.dots.first { it.day == 1 }
        assertEquals(0, day1.row)
        assertEquals(2, day1.col)
        assertTrue(!day1.isToday)

        val day14 = grid.dots.first { it.day == 14 }
        assertEquals(2, day14.row)
        assertEquals(1, day14.col)
        assertTrue(day14.isToday)

        val day30 = grid.dots.first { it.day == 30 }
        assertEquals(4, day30.row)
        assertEquals(3, day30.col)
        assertEquals(5, grid.rows)
    }

    @Test
    fun `dot flash is off far from its peak`() {
        assertEquals(0f, dotFlashIntensity(progress = 0f, peak = 0.5f, pulseWidth = 0.15f), 0f)
        assertEquals(0f, dotFlashIntensity(progress = 1f, peak = 0.5f, pulseWidth = 0.15f), 0f)
    }

    @Test
    fun `dot flash peaks fully at its peak progress`() {
        assertEquals(1f, dotFlashIntensity(progress = 0.5f, peak = 0.5f, pulseWidth = 0.15f), 0.0001f)
    }

    @Test
    fun `dot flash ramps up and down linearly around its peak`() {
        // Halfway through the rising edge (peak - pulseWidth/2).
        assertEquals(0.5f, dotFlashIntensity(progress = 0.425f, peak = 0.5f, pulseWidth = 0.15f), 0.0001f)
        // Halfway through the falling edge (peak + pulseWidth/2).
        assertEquals(0.5f, dotFlashIntensity(progress = 0.575f, peak = 0.5f, pulseWidth = 0.15f), 0.0001f)
    }

    @Test
    fun `dot flash reaches exactly zero at the edges of its pulse width`() {
        assertEquals(0f, dotFlashIntensity(progress = 0.35f, peak = 0.5f, pulseWidth = 0.15f), 0.0001f)
        assertEquals(0f, dotFlashIntensity(progress = 0.65f, peak = 0.5f, pulseWidth = 0.15f), 0.0001f)
    }
}
