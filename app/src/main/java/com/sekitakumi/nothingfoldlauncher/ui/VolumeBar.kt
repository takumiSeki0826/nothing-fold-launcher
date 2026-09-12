package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

private const val TOTAL_DOTS = 20

@Composable
fun VolumeBar(
    ratio: Float,
    onRatioChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    onRatioChange(dragPositionToRatio(change.position.x, size.width.toFloat()))
                }
            },
    ) {
        val litCount = litDotCount(ratio, TOTAL_DOTS)
        val cellWidth = size.width / TOTAL_DOTS
        val dotRadius = cellWidth * 0.3f

        for (i in 0 until TOTAL_DOTS) {
            val color = if (i < litCount) Color.White else Color.DarkGray
            drawCircle(
                color = color,
                radius = dotRadius,
                center = Offset(x = cellWidth * i + cellWidth / 2f, y = size.height / 2f),
            )
        }
    }
}
