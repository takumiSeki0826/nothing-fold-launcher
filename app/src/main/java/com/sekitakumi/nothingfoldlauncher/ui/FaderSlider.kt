package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingGrays

private const val THUMB_ENTRANCE_START_DP = -110f

val DEFAULT_FADER_TRACK_HEIGHT = 160.dp

@Composable
fun FaderSlider(
    label: String,
    ratio: Float,
    onRatioChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    trackHeight: Dp = DEFAULT_FADER_TRACK_HEIGHT,
) {
    val thumbEntranceOffsetDp = remember { Animatable(THUMB_ENTRANCE_START_DP) }
    LaunchedEffect(Unit) {
        thumbEntranceOffsetDp.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioHighBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        )
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        DotMatrixText(text = label, color = Color.Gray, fontSize = 11.sp)
        Canvas(
            modifier = Modifier
                .padding(top = 10.dp)
                .width(56.dp)
                .height(trackHeight)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, _ ->
                        onRatioChange(verticalDragToRatio(change.position.y, size.height.toFloat()))
                    }
                },
        ) {
            val thumbOffsetPx = thumbEntranceOffsetDp.value.dp.toPx()
            val grooveWidth = 6.dp.toPx()
            val centerX = size.width / 2f
            val grooveCorner = CornerRadius(grooveWidth / 2f, grooveWidth / 2f)

            // Groove track
            drawRoundRect(
                color = NothingGrays.Base,
                topLeft = Offset(centerX - grooveWidth / 2f, 0f),
                size = Size(grooveWidth, size.height),
                cornerRadius = grooveCorner,
            )

            // Fill up to the current value
            val filledHeight = size.height * ratio
            drawRoundRect(
                color = Color(0xFF888888),
                topLeft = Offset(centerX - grooveWidth / 2f, size.height - filledHeight),
                size = Size(grooveWidth, filledHeight),
                cornerRadius = grooveCorner,
            )

            // Fader thumb (a wide cap, 3/4 of the full width)
            val thumbHeight = 10.dp.toPx()
            val thumbWidth = size.width * 0.75f
            val thumbX = (size.width - thumbWidth) / 2f
            val thumbY = (size.height * (1f - ratio) - thumbHeight / 2f)
                .coerceIn(0f, size.height - thumbHeight) + thumbOffsetPx
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(thumbX, thumbY),
                size = Size(thumbWidth, thumbHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            )
            drawLine(
                color = NothingGrays.Base,
                start = Offset(thumbX + 4.dp.toPx(), thumbY + thumbHeight / 2f),
                end = Offset(thumbX + thumbWidth - 4.dp.toPx(), thumbY + thumbHeight / 2f),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}
