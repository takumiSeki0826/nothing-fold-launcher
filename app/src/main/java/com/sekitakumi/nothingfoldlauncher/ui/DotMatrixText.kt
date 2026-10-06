package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit

/**
 * グリフのある文字（数字・英字・`° % . , - : /`・空白。小文字は大文字で描く）をドットマトリクス書体で描き、それ以外は通常の [Text] で描く。
 * 数字の高さは [fontSize] から決まる（[DIGIT_HEIGHT_RATIO]）。
 */
@Composable
fun DotMatrixText(
    text: String,
    fontSize: TextUnit,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val fontSizePx = with(LocalDensity.current) { fontSize.toPx() }
    val d = dotMatrixDotSizePx(fontSizePx)
    val pitch = d * DOT_PITCH_RATIO

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        splitDotRuns(text).forEach { run ->
            if (run.isDot) {
                val widthPx = dotMatrixRunWidthPx(run.text, d)
                val heightPx = 10f * d
                Canvas(
                    modifier = Modifier.size(
                        width = with(LocalDensity.current) { widthPx.toDp() },
                        height = with(LocalDensity.current) { heightPx.toDp() },
                    ),
                ) {
                    var charX = 0f
                    run.text.forEach { char ->
                        dotMatrixGlyph(char).forEachIndexed { row, line ->
                            line.forEachIndexed { col, cell ->
                                if (cell == '#') {
                                    drawCircle(
                                        color = color,
                                        radius = d / 2f,
                                        center = Offset(charX + col * pitch + d / 2f, row * pitch + d / 2f),
                                    )
                                }
                            }
                        }
                        charX += dotMatrixGlyphWidthPx(char, d) + pitch
                    }
                }
            } else {
                Text(text = run.text, color = color, fontSize = fontSize)
            }
        }
    }
}
