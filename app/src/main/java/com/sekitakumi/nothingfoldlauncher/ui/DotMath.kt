package com.sekitakumi.nothingfoldlauncher.ui

import kotlin.math.roundToInt

fun dotRadiusRatio(luminance: Float, minRatio: Float = 0.15f, maxRatio: Float = 0.9f): Float {
    val clamped = luminance.coerceIn(0f, 1f)
    return minRatio + (maxRatio - minRatio) * clamped
}

fun litDotCount(ratio: Float, totalDots: Int): Int {
    val clamped = ratio.coerceIn(0f, 1f)
    return (clamped * totalDots).roundToInt().coerceIn(0, totalDots)
}

fun dragPositionToRatio(x: Float, width: Float): Float {
    if (width <= 0f) return 0f
    return (x / width).coerceIn(0f, 1f)
}

fun verticalDragToRatio(y: Float, height: Float): Float {
    if (height <= 0f) return 0f
    return (1f - y / height).coerceIn(0f, 1f)
}
