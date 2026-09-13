package com.sekitakumi.nothingfoldlauncher.ui

import java.time.LocalDate
import java.time.YearMonth

fun daysInMonth(year: Int, month: Int): Int = YearMonth.of(year, month).lengthOfMonth()

/** Returns the weekday of the 1st of the month, 0 = Sunday */
fun firstWeekdayOffset(year: Int, month: Int): Int =
    LocalDate.of(year, month, 1).dayOfWeek.value % 7
