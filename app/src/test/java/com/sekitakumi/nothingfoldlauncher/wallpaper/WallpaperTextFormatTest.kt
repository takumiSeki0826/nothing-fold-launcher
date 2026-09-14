package com.sekitakumi.nothingfoldlauncher.wallpaper

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class WallpaperTextFormatTest {

    private fun dateOf(year: Int, month: Int, day: Int, hour: Int, minute: Int): java.util.Date {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.set(year, month - 1, day, hour, minute, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    @Test
    fun `formats time as zero-padded 24-hour HH-mm`() {
        assertEquals("09:05", timeText(dateOf(2026, 9, 13, 9, 5)))
        assertEquals("23:59", timeText(dateOf(2026, 9, 13, 23, 59)))
    }

    @Test
    fun `formats short date with uppercase English weekday abbreviation`() {
        // 2026-09-13 is a Sunday
        assertEquals("09.13 SUN", dateWithWeekdayText(dateOf(2026, 9, 13, 0, 0)))
        // 2026-09-14 is a Monday
        assertEquals("09.14 MON", dateWithWeekdayText(dateOf(2026, 9, 14, 0, 0)))
    }

}
