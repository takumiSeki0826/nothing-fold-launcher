package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val JOG_WHEEL_OUTER_COLOR = Color(0xFFD1432B)
private val JOG_WHEEL_SPINDLE_COLOR = Color.White
private val JOG_WHEEL_YMARK_COLOR = Color.White
private val JOG_WHEEL_GLOW_COLOR = Color.White
private const val JOG_WHEEL_GLOW_ARC_WIDTH_DEG = 36f
private const val JOG_WHEEL_SPINDLE_RADIUS_RATIO = 0.16f
private const val JOG_WHEEL_YMARK_INNER_RATIO = 0.34f
private const val JOG_WHEEL_YMARK_OUTER_RATIO = 0.80f
private val JOG_WHEEL_YMARK_TICK_LENGTH = 7.dp
private val JOG_WHEEL_YMARK_TICK_THICKNESS = 3.dp
private val JOG_WHEEL_YMARK_TICK_PITCH = 8.dp
private const val JOG_WHEEL_YMARK_OUTER_ALPHA = 0.12f
private val JOG_WHEEL_YMARK_ANGLES_DEG = listOf(0f, 120f, 240f)
private const val JOG_WHEEL_ROTATION_DURATION_MS = 6000
private val JOG_WHEEL_BUTTON_DIAMETER = 56.dp
val DEFAULT_JOG_WHEEL_DIAMETER = 330.dp

@Composable
fun AlphabetJogWheel(
    onLetterSelected: (Char) -> Unit,
    onClearSearch: () -> Unit,
    onJumpToStart: () -> Unit,
    onJumpToEnd: () -> Unit,
    systemStats: SystemStatsState,
    onSystemStatsNetClick: () -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    diameter: Dp = DEFAULT_JOG_WHEEL_DIAMETER,
) {
    var displayedLetter by remember { mutableStateOf('#') }
    var isDragging by remember { mutableStateOf(false) }
    var glowAngleDeg by remember { mutableFloatStateOf(0f) }
    var reversed by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(isPlaying) {
        // Hold the current angle while paused; resume from it when playback restarts.
        while (isPlaying) {
            val start = rotation.value % 360f
            rotation.snapTo(start)
            rotation.animateTo(
                targetValue = start + 360f,
                animationSpec = tween(JOG_WHEEL_ROTATION_DURATION_MS, easing = LinearEasing),
            )
        }
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(diameter).graphicsLayer { rotationZ = rotation.value }) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val discRadius = size.minDimension / 2f

            drawCircle(color = JOG_WHEEL_OUTER_COLOR, radius = discRadius, center = center)
            drawCircle(
                color = JOG_WHEEL_SPINDLE_COLOR,
                radius = discRadius * JOG_WHEEL_SPINDLE_RADIUS_RATIO,
                center = center,
            )

            val innerRadius = discRadius * JOG_WHEEL_YMARK_INNER_RATIO
            val outerRadius = discRadius * JOG_WHEEL_YMARK_OUTER_RATIO
            val tickPitchPx = JOG_WHEEL_YMARK_TICK_PITCH.toPx()
            val tickHalfLengthPx = JOG_WHEEL_YMARK_TICK_LENGTH.toPx() / 2f
            val tickCount = ((outerRadius - innerRadius) / tickPitchPx).toInt() + 1
            for (angle in JOG_WHEEL_YMARK_ANGLES_DEG) {
                val along = angleToIndicatorOffset(angle, 1f)
                val across = Offset(-along.y, along.x)
                for (i in 0 until tickCount) {
                    val t = i * tickPitchPx / (outerRadius - innerRadius)
                    val tickCenter = center + along * (innerRadius + i * tickPitchPx)
                    drawLine(
                        color = JOG_WHEEL_YMARK_COLOR.copy(alpha = 1f - (1f - JOG_WHEEL_YMARK_OUTER_ALPHA) * t),
                        start = tickCenter - across * tickHalfLengthPx,
                        end = tickCenter + across * tickHalfLengthPx,
                        strokeWidth = JOG_WHEEL_YMARK_TICK_THICKNESS.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
        }

        Canvas(
            modifier = Modifier
                .size(diameter)
                .pointerInput(Unit) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    fun angleAt(position: Offset) =
                        angleDegFromCenterOffset(dx = position.x - center.x, dy = position.y - center.y)
                    fun letterAt(angleDeg: Float) = letterForWheelAngleDeg(angleDeg, reversed)

                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        isDragging = true
                        var angle = angleAt(down.position)
                        glowAngleDeg = angle
                        var currentLetter = letterAt(angle)
                        displayedLetter = currentLetter
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onLetterSelected(currentLetter)

                        var pointerId = down.id
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (!change.pressed) break
                            change.consume()
                            angle = angleAt(change.position)
                            glowAngleDeg = angle
                            val letter = letterAt(angle)
                            if (letter != currentLetter) {
                                currentLetter = letter
                                displayedLetter = letter
                                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                onLetterSelected(letter)
                            }
                            pointerId = change.id
                        }
                        isDragging = false
                    }
                },
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val discRadius = size.minDimension / 2f

            if (isDragging) {
                val arcRadius = discRadius * 0.94f
                val arcTopLeft = Offset(center.x - arcRadius, center.y - arcRadius)
                val arcSize = Size(arcRadius * 2f, arcRadius * 2f)
                val startAngle = jogWheelArcStartAngleDeg(glowAngleDeg, JOG_WHEEL_GLOW_ARC_WIDTH_DEG)
                val layers = listOf(14.dp.toPx() to 0.18f, 9.dp.toPx() to 0.4f, 5.dp.toPx() to 0.95f)
                for ((strokeWidthPx, alpha) in layers) {
                    drawArc(
                        color = JOG_WHEEL_GLOW_COLOR.copy(alpha = alpha),
                        startAngle = startAngle,
                        sweepAngle = JOG_WHEEL_GLOW_ARC_WIDTH_DEG,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                    )
                }
            }
        }

        DotMatrixText(
            text = displayedLetter.toString(),
            color = if (isDragging) Color.Black else Color.Black.copy(alpha = 0.6f),
            fontSize = if (isDragging) 44.sp else 30.sp,
        )

        RotaryKnob(
            angleDeg = 0f,
            label = "CLR",
            onTap = onClearSearch,
            onLongPress = {},
            diameter = JOG_WHEEL_BUTTON_DIAMETER,
            modifier = Modifier.align(Alignment.TopStart),
        )

        Column(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RotaryKnob(
                angleDeg = 0f,
                label = "TOP",
                onTap = onJumpToStart,
                onLongPress = {},
                diameter = JOG_WHEEL_BUTTON_DIAMETER,
            )
            RotaryKnob(
                angleDeg = 0f,
                label = "END",
                onTap = onJumpToEnd,
                onLongPress = {},
                diameter = JOG_WHEEL_BUTTON_DIAMETER,
            )
        }

        RotaryKnob(
            angleDeg = if (reversed) 180f else 0f,
            label = "REV",
            onTap = { reversed = !reversed },
            onLongPress = {},
            diameter = JOG_WHEEL_BUTTON_DIAMETER,
            modifier = Modifier.align(Alignment.BottomEnd),
        )

        SystemStatsWidget(
            stats = systemStats,
            onNetClick = onSystemStatsNetClick,
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }
}
