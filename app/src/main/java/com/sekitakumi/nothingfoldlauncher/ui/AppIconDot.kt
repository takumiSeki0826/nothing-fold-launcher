package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

private val APP_ICON_DOT_COLOR = Color(0xFFD1432B)
private val APP_ICON_DOT_SIZE = 8.dp
private val APP_ICON_DOT_INSET = 4.dp
private const val APP_ICON_DOT_DROP_DISTANCE_MAX_DP = 44f
private const val APP_ICON_DOT_DROP_DISTANCE_MIN_FRACTION = 0.7f

// The two bounces stay well below the top (as a fraction of the drop distance, where
// 1f is the floor and 0f is the top) so the motion clearly reads as landing near the
// bottom, before the final segment does the one big climb back up to the top.
private const val APP_ICON_DOT_FIRST_BOUNCE_DEPTH_FRACTION = 0.55f
private const val APP_ICON_DOT_SECOND_IMPACT_DEPTH_FRACTION = 0.78f

private const val APP_ICON_DOT_FALL_DURATION_MAX_MS = 260
private const val APP_ICON_DOT_SMALL_BOUNCE_DURATION_MAX_MS = 180
private const val APP_ICON_DOT_FINAL_RISE_DURATION_MAX_MS = 320

// Falls accelerate like gravity; rises decelerate like leaving the floor against gravity.
private val APP_ICON_DOT_FALL_EASING = CubicBezierEasing(0.55f, 0f, 1f, 0.45f)
private val APP_ICON_DOT_RISE_EASING = CubicBezierEasing(0f, 0.55f, 0.45f, 1f)

internal fun scaledDurationMs(distanceDp: Float, maxDurationMs: Int): Int =
    (maxDurationMs * sqrt((distanceDp / APP_ICON_DOT_DROP_DISTANCE_MAX_DP).coerceIn(0.05f, 1f)))
        .roundToInt()
        .coerceAtLeast(60)

/**
 * The orange dot shown at the top-right corner of an app icon tile, wherever that
 * tile appears (home grid, folder overlay). Plays a fall-and-bounce landing
 * animation once whenever it enters composition.
 */
@Composable
fun AppIconDot(modifier: Modifier = Modifier) {
    val dotWobbleOffset = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        val dropDistance = APP_ICON_DOT_DROP_DISTANCE_MAX_DP * (
            APP_ICON_DOT_DROP_DISTANCE_MIN_FRACTION +
                Random.nextFloat() * (1f - APP_ICON_DOT_DROP_DISTANCE_MIN_FRACTION)
            )
        val firstBounceDepth = dropDistance * APP_ICON_DOT_FIRST_BOUNCE_DEPTH_FRACTION
        val secondImpactDepth = dropDistance * APP_ICON_DOT_SECOND_IMPACT_DEPTH_FRACTION

        // Fall from the top like a dropped ball, accelerating into the floor.
        dotWobbleOffset.animateTo(
            targetValue = dropDistance,
            animationSpec = tween(
                durationMillis = scaledDurationMs(dropDistance, APP_ICON_DOT_FALL_DURATION_MAX_MS),
                easing = APP_ICON_DOT_FALL_EASING,
            ),
        )
        // Small bounce off the floor, then a smaller second impact - both still
        // near the bottom.
        dotWobbleOffset.animateTo(
            targetValue = firstBounceDepth,
            animationSpec = tween(
                durationMillis = scaledDurationMs(dropDistance - firstBounceDepth, APP_ICON_DOT_SMALL_BOUNCE_DURATION_MAX_MS),
                easing = APP_ICON_DOT_RISE_EASING,
            ),
        )
        dotWobbleOffset.animateTo(
            targetValue = secondImpactDepth,
            animationSpec = tween(
                durationMillis = scaledDurationMs(secondImpactDepth - firstBounceDepth, APP_ICON_DOT_SMALL_BOUNCE_DURATION_MAX_MS),
                easing = APP_ICON_DOT_FALL_EASING,
            ),
        )
        // Then climb all the way back up to rest at the top.
        dotWobbleOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = scaledDurationMs(secondImpactDepth, APP_ICON_DOT_FINAL_RISE_DURATION_MAX_MS),
                easing = APP_ICON_DOT_RISE_EASING,
            ),
        )
    }
    Box(
        modifier = modifier
            .padding(top = APP_ICON_DOT_INSET, end = APP_ICON_DOT_INSET)
            .offset(y = dotWobbleOffset.value.dp)
            .size(APP_ICON_DOT_SIZE)
            .background(APP_ICON_DOT_COLOR, CircleShape),
    )
}
