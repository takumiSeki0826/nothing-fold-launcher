package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

private const val ENTRANCE_WOBBLE_STAGGER_MS = 40L

fun angleToIndicatorOffset(angleDeg: Float, radiusPx: Float): Offset {
    val radians = Math.toRadians((angleDeg - 90.0))
    return Offset(radiusPx * cos(radians).toFloat(), radiusPx * sin(radians).toFloat())
}

fun entranceWobbleDelayMs(orderIndex: Int): Long = orderIndex * ENTRANCE_WOBBLE_STAGGER_MS
