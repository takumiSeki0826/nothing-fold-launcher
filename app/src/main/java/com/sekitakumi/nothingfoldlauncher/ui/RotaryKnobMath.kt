package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.ui.geometry.Offset
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val ENTRANCE_WOBBLE_STAGGER_MS = 40L

fun angleToIndicatorOffset(angleDeg: Float, radiusPx: Float): Offset {
    val radians = Math.toRadians((angleDeg - 90.0))
    return Offset(radiusPx * cos(radians).toFloat(), radiusPx * sin(radians).toFloat())
}

// Inverse of angleToIndicatorOffset: given a touch point's offset from the
// knob's center, returns the angle (0deg = straight up, clockwise positive).
fun angleDegFromCenterOffset(dx: Float, dy: Float): Float {
    val degrees = Math.toDegrees(atan2(dy, dx).toDouble()) + 90.0
    return ((degrees % 360.0) + 360.0).toFloat() % 360f
}

// Converts a wheel angle (0deg = straight up, clockwise positive) to the
// start angle of a Compose drawArc sweep (0deg = 3 o'clock, clockwise
// positive) for an arc of the given width centered on that wheel angle.
fun jogWheelArcStartAngleDeg(centerAngleDeg: Float, arcWidthDeg: Float): Float =
    centerAngleDeg - arcWidthDeg / 2f - 90f

fun entranceWobbleDelayMs(orderIndex: Int): Long = orderIndex * ENTRANCE_WOBBLE_STAGGER_MS
