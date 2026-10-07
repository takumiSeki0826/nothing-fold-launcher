package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.ui.unit.dp

const val CALENDAR_GRID_COLUMNS = 7

// Dot radius shared by the calendar and now playing widgets so their dots match.
val DOT_RADIUS = 3.5.dp
val COMPACT_DOT_RADIUS = 4.0.dp

data class CalendarDot(val day: Int, val row: Int, val col: Int, val isToday: Boolean)

data class CalendarDotGrid(val columns: Int, val rows: Int, val dots: List<CalendarDot>)

/**
 * Pure layout for the dot-matrix mini calendar, shared by the home screen
 * CalendarWidget composable and the Canvas-based wallpaper renderers so both
 * draw the exact same grid.
 */
fun calendarDotGrid(year: Int, month: Int, today: Int): CalendarDotGrid {
    val totalDays = daysInMonth(year, month)
    val offset = firstWeekdayOffset(year, month)
    val rows = ((offset + totalDays - 1) / CALENDAR_GRID_COLUMNS) + 1
    val dots = (1..totalDays).map { day ->
        val cellIndex = offset + day - 1
        CalendarDot(
            day = day,
            row = cellIndex / CALENDAR_GRID_COLUMNS,
            col = cellIndex % CALENDAR_GRID_COLUMNS,
            isToday = day == today,
        )
    }
    return CalendarDotGrid(CALENDAR_GRID_COLUMNS, rows, dots)
}

/**
 * How strongly a single dot should be tinted towards its "lit" color during
 * the home screen's random sparkle animation, driven by one shared
 * [progress] (0f..1f) so an entire grid of dots can flash at different
 * moments without each needing its own animation. The dot is untinted until
 * [progress] nears [peak], ramps up linearly to fully lit exactly at [peak],
 * then ramps back down to untinted by the time [progress] is [pulseWidth]
 * past it.
 */
fun dotFlashIntensity(progress: Float, peak: Float, pulseWidth: Float): Float =
    (1f - kotlin.math.abs(progress - peak) / pulseWidth).coerceIn(0f, 1f)
