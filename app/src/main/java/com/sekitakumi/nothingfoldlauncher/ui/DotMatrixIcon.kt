package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingGrays

/**
 * [DotIcons] などの図柄をドットで描く。ドット径・ピッチは [DotMatrixText] のグリフと同じ比率。
 * `#` は [color]、`x` は [color] を [OUTLINE_ALPHA] に薄めた色、`o` は [dimColor] で描く。
 */
@Composable
fun DotMatrixIcon(
    rows: List<String>,
    color: Color,
    modifier: Modifier = Modifier,
    dotSize: Dp = 1.5.dp,
    dimColor: Color = NothingGrays.OnBase,
) {
    val density = LocalDensity.current
    val d = with(density) { dotSize.toPx() }
    val pitch = d * DOT_PITCH_RATIO
    Canvas(
        modifier = modifier.size(
            width = with(density) { dotMatrixIconWidthPx(rows, d).toDp() },
            height = with(density) { dotMatrixIconHeightPx(rows, d).toDp() },
        ),
    ) {
        rows.forEachIndexed { row, line ->
            line.forEachIndexed { col, cell ->
                if (cell != '.') {
                    drawCircle(
                        color = when (cell) {
                            '#' -> color
                            'x' -> color.copy(alpha = OUTLINE_ALPHA)
                            else -> dimColor
                        },
                        radius = d / 2f,
                        center = Offset(col * pitch + d / 2f, row * pitch + d / 2f),
                    )
                }
            }
        }
    }
}

/** `x` ドット(バッテリーの枠など)の不透明度。 */
private const val OUTLINE_ALPHA = 0.45f
