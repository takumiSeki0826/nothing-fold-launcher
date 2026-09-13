package com.sekitakumi.nothingfoldlauncher.wallpaper

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun timeText(date: Date): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)

fun dateWithWeekdayText(date: Date): String =
    SimpleDateFormat("MM.dd EEE", Locale.ENGLISH).format(date).uppercase(Locale.ENGLISH)

fun monthYearText(date: Date): String =
    SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(date).uppercase(Locale.ENGLISH)

fun weekdayFullText(date: Date): String =
    SimpleDateFormat("EEEE", Locale.ENGLISH).format(date).uppercase(Locale.ENGLISH)

fun dayOfMonthText(date: Date): String =
    SimpleDateFormat("d", Locale.ENGLISH).format(date)
