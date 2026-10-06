package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

/**
 * グリフのある文字（数字・英字・記号。小文字は大文字で描く）をドットマトリクス書体で描き、
 * それ以外（日本語など）は通常の [Text] で描く。数字の高さは [fontSize] から決まる（[DIGIT_HEIGHT_RATIO]）。
 *
 * [ellipsize] が true のときは1行に収め、使える幅を超える分を「…」にする。
 * [textAlign] が [TextAlign.Center] / [TextAlign.End] のときは、親の幅いっぱいに広げて寄せる。
 */
@Composable
fun DotMatrixText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = LocalTextStyle.current.fontSize.takeIf { it.isSpecified } ?: 14.sp,
    color: Color = LocalContentColor.current,
    letterSpacing: TextUnit = 0.sp,
    textAlign: TextAlign = TextAlign.Start,
    ellipsize: Boolean = false,
) {
    val density = LocalDensity.current
    val fontSizePx = with(density) { fontSize.toPx() }
    val d = dotMatrixDotSizePx(fontSizePx)
    val extraGapPx = if (letterSpacing.isSpecified) with(density) { letterSpacing.toPx() } else 0f

    if (!ellipsize) {
        DotMatrixTextRow(text, modifier, fontSize, color, letterSpacing, textAlign, d, extraGapPx)
        return
    }
    BoxWithConstraints(modifier = modifier) {
        val shown = if (constraints.hasBoundedWidth) {
            truncateForWidth(text, d, fontSizePx, constraints.maxWidth.toFloat(), extraGapPx)
        } else {
            text
        }
        DotMatrixTextRow(shown, Modifier, fontSize, color, letterSpacing, textAlign, d, extraGapPx)
    }
}

@Composable
private fun DotMatrixTextRow(
    text: String,
    modifier: Modifier,
    fontSize: TextUnit,
    color: Color,
    letterSpacing: TextUnit,
    textAlign: TextAlign,
    d: Float,
    extraGapPx: Float,
) {
    val density = LocalDensity.current
    val advanceGap = d * DOT_PITCH_RATIO + extraGapPx
    val aligned = when (textAlign) {
        TextAlign.Center -> Modifier.fillMaxWidth()
        TextAlign.End, TextAlign.Right -> Modifier.fillMaxWidth()
        else -> Modifier
    }
    val arrangement = when (textAlign) {
        TextAlign.Center -> Arrangement.Center
        TextAlign.End, TextAlign.Right -> Arrangement.End
        else -> Arrangement.Start
    }

    Row(
        modifier = modifier.then(aligned),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        splitDotRuns(text).forEach { run ->
            if (run.isDot) {
                val widthPx = dotMatrixRunWidthPx(run.text, d, extraGapPx)
                val heightPx = 10f * d
                Canvas(
                    modifier = Modifier.size(
                        width = with(density) { widthPx.toDp() },
                        height = with(density) { heightPx.toDp() },
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
                                        center = Offset(
                                            charX + col * d * DOT_PITCH_RATIO + d / 2f,
                                            row * d * DOT_PITCH_RATIO + d / 2f,
                                        ),
                                    )
                                }
                            }
                        }
                        charX += dotMatrixGlyphWidthPx(char, d) + advanceGap
                    }
                }
            } else {
                Text(text = run.text, color = color, fontSize = fontSize, letterSpacing = letterSpacing)
            }
        }
    }
}
