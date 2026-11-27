package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingGrays

val DEFAULT_KNOB_DIAMETER = 150.dp
private val KNOB_FILL_COLOR = NothingGrays.Base
private val KNOB_DOT_COLOR = Color(0xFFD1432B)
private val KNOB_DOT_RADIUS = 4.dp
private const val DOT_BASE_ANGLE_OFFSET_DEG = 30f
private const val PRESS_ROTATION_DEG = 180f

// Same shape and total length as the app-icon tile's dot-drop reveal
// (HomeScreen.kt's AppIconTile), just applied to an angle instead of a
// pixel offset, so knobs and app tiles read as one consistent "dropped
// ball" motion: fall in, two decaying bounces near the bottom, one climb
// back up to rest.
private const val FALL_DURATION_MS = 260
private const val SMALL_BOUNCE_DURATION_MS = 180
private const val FINAL_RISE_DURATION_MS = 320
private const val FIRST_BOUNCE_DEPTH_FRACTION = 0.55f
private const val SECOND_IMPACT_DEPTH_FRACTION = 0.78f
private val FALL_EASING = CubicBezierEasing(0.55f, 0f, 1f, 0.45f)
private val RISE_EASING = CubicBezierEasing(0f, 0.55f, 0.45f, 1f)

// Shared by the press feedback and by every screen-transition entrance, so the dot
// reads the same "dropped and bounced back" way everywhere it's used.
private suspend fun Animatable<Float, AnimationVector1D>.playFallAndBounce() {
    val firstBounceDeg = PRESS_ROTATION_DEG * FIRST_BOUNCE_DEPTH_FRACTION
    val secondImpactDeg = PRESS_ROTATION_DEG * SECOND_IMPACT_DEPTH_FRACTION

    // Fall like a dropped ball, accelerating into the floor.
    animateTo(PRESS_ROTATION_DEG, tween(durationMillis = FALL_DURATION_MS, easing = FALL_EASING))
    // Small bounce off the floor, then a smaller second impact - both still near the bottom.
    animateTo(firstBounceDeg, tween(durationMillis = SMALL_BOUNCE_DURATION_MS, easing = RISE_EASING))
    animateTo(secondImpactDeg, tween(durationMillis = SMALL_BOUNCE_DURATION_MS, easing = FALL_EASING))
    // Then climb all the way back up to rest.
    animateTo(0f, tween(durationMillis = FINAL_RISE_DURATION_MS, easing = RISE_EASING))
}

@Composable
fun RotaryKnob(
    angleDeg: Float,
    label: String,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    diameter: Dp = DEFAULT_KNOB_DIAMETER,
    entranceWobbleDelayMs: Long = 0L,
    clickable: Boolean = true,
    pressed: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isGlowing = isHovered || isPressed || pressed

    // `clickable = false` callers (e.g. grid tiles with their own drag-reorder gesture)
    // never feed `interactionSource`, so they report press state through `pressed` instead.
    val dotPressed = isPressed || pressed
    val pressRotationDeg = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(dotPressed) {
        // Fired once per press-down and left to run to completion on its own scope,
        // so a quick tap still plays the full fall-and-bounce instead of being cut
        // short by the release triggering this effect's cancellation.
        if (dotPressed) {
            coroutineScope.launch { pressRotationDeg.playFallAndBounce() }
        }
    }

    // Every screen transition (fold/unfold, navigating screens, ...) tears down and
    // re-mounts this composable, so this fires fresh each time and gives the dot the
    // same fall-and-bounce entrance everywhere it (re)appears.
    LaunchedEffect(Unit) {
        delay(entranceWobbleDelayMs)
        pressRotationDeg.playFallAndBounce()
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            modifier = Modifier
                .size(diameter)
                .hoverable(interactionSource)
                .then(
                    if (clickable) {
                        Modifier.combinedClickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onTap,
                            onLongClick = onLongPress,
                        )
                    } else {
                        Modifier
                    },
                ),
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val knobRadius = size.minDimension / 2f

            if (isGlowing) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                        center = center,
                        radius = knobRadius * 1.6f,
                    ),
                    radius = knobRadius * 1.6f,
                    center = center,
                )
            }

            drawCircle(color = KNOB_FILL_COLOR, radius = knobRadius, center = center)

            val dotOffset = angleToIndicatorOffset(
                angleDeg + DOT_BASE_ANGLE_OFFSET_DEG + pressRotationDeg.value,
                knobRadius * 0.55f,
            )
            drawCircle(color = KNOB_DOT_COLOR, radius = KNOB_DOT_RADIUS.toPx(), center = center + dotOffset)
        }
        DotMatrixText(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
