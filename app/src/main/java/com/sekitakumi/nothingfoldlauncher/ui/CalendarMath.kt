package com.sekitakumi.nothingfoldlauncher.ui

import java.time.LocalDate
import java.time.YearMonth

fun daysInMonth(year: Int, month: Int): Int = YearMonth.of(year, month).lengthOfMonth()

/** 0=日曜日始まりで、その月の1日が何曜日かを返す */
fun firstWeekdayOffset(year: Int, month: Int): Int =
    LocalDate.of(year, month, 1).dayOfWeek.value % 7
