package com.sekitakumi.nothingfoldlauncher.wallpaper

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun timeText(date: Date): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)

fun dateWithWeekdayText(date: Date): String =
    SimpleDateFormat("MM.dd EEE", Locale.ENGLISH).format(date).uppercase(Locale.ENGLISH)
