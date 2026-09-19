package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
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
private const val PRESS_ROTATION_DEG = 270f
private const val PRESS_ROTATION_DURATION_MS = 500
private const val ENTRANCE_WOBBLE_DEG = 14f

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
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isGlowing = isHovered || isPressed

    val pressRotationDeg = remember { Animatable(0f) }
    LaunchedEffect(isPressed) {
        if (isPressed) {
            pressRotationDeg.animateTo(
                targetValue = PRESS_ROTATION_DEG,
                animationSpec = tween(durationMillis = PRESS_ROTATION_DURATION_MS, easing = LinearEasing),
            )
        } else {
            pressRotationDeg.animateTo(
                targetValue = 0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            )
        }
    }

    val entranceWobbleDeg = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(entranceWobbleDelayMs)
        entranceWobbleDeg.animateTo(
            targetValue = ENTRANCE_WOBBLE_DEG,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        )
        entranceWobbleDeg.animateTo(
            targetValue = 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        )
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

            drawCircle(color = KNOB_FILL_COLOR, radius = knobRadius * 0.85f, center = center)

            val dotOffset = angleToIndicatorOffset(
                angleDeg + pressRotationDeg.value + entranceWobbleDeg.value,
                knobRadius * 0.55f,
            )
            drawCircle(color = KNOB_DOT_COLOR, radius = KNOB_DOT_RADIUS.toPx(), center = center + dotOffset)
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
