package com.sekitakumi.nothingfoldlauncher.ui

const val CALENDAR_GRID_COLUMNS = 7

/**
 * Fraction of the grid's drawing area left blank above the dots, so the grid's
 * visual weight sits a little lower instead of dead-centered. Shared by the
 * CalendarWidget composable and the Canvas-based wallpaper renderers.
 */
const val CALENDAR_GRID_TOP_INSET_RATIO = 0.12f

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
