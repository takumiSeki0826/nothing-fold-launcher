package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarMathTest {

    @Test
    fun `daysInMonth returns correct day count`() {
        assertEquals(31, daysInMonth(2026, 1))
        assertEquals(28, daysInMonth(2026, 2))
        assertEquals(29, daysInMonth(2028, 2))
        assertEquals(30, daysInMonth(2026, 4))
    }

    @Test
    fun `firstWeekdayOffset returns 0 for Sunday start`() {
        // 2026-02-01 is a Sunday
        assertEquals(0, firstWeekdayOffset(2026, 2))
        // 2026-09-01 is a Tuesday
        assertEquals(2, firstWeekdayOffset(2026, 9))
    }
}
