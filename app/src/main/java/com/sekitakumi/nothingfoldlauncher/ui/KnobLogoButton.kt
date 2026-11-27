package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val LOGO_SPAN_FRACTION = 0.72f
private const val LOGO_DOT_RADIUS_FRACTION = 0.36f
private const val PRESSED_ALPHA = 0.5f

/**
 * A bare dot-matrix logo that launches an app; [touchSize] is the whole tap target.
 * No visible label: [contentDescription] carries the app name for screen readers.
 */
@Composable
fun KnobLogoButton(
    logo: KnobLogo,
    contentDescription: String,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    touchSize: Dp,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val dotColor = if (isPressed) Color.White.copy(alpha = PRESSED_ALPHA) else Color.White

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            modifier = Modifier
                .size(touchSize)
                .semantics { this.contentDescription = contentDescription }
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onTap,
                    onLongClick = onLongPress,
                ),
        ) {
            drawLogoDots(logo.pattern, dotColor)
        }
    }
}

private fun DrawScope.drawLogoDots(rows: List<String>, color: Color) {
    val cols = rows.first().length
    val cell = size.minDimension * LOGO_SPAN_FRACTION / maxOf(rows.size, cols)
    val originX = (size.width - cell * cols) / 2f
    val originY = (size.height - cell * rows.size) / 2f
    for (r in rows.indices) {
        for (c in rows[r].indices) {
            if (rows[r][c] == '#') {
                drawCircle(
                    color = color,
                    radius = cell * LOGO_DOT_RADIUS_FRACTION,
                    center = Offset(originX + (c + 0.5f) * cell, originY + (r + 0.5f) * cell),
                )
            }
        }
    }
}
