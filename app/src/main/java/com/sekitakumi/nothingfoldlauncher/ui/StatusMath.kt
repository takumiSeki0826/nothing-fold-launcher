package com.sekitakumi.nothingfoldlauncher.ui

fun batteryPercent(level: Int, scale: Int): Int {
    if (scale <= 0) return 0
    return ((level * 100) / scale).coerceIn(0, 100)
}

fun signalBars(level: Int): Int = level.coerceIn(0, 4)
