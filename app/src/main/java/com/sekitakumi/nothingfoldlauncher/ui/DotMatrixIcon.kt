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
 * `#` は [color]、`x` は [color] のまま径を [OUTLINE_DOT_SCALE] 倍に縮めて、`o` は [dimColor] で描く。
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
                    val scale = if (cell == 'x') OUTLINE_DOT_SCALE else 1f
                    drawCircle(
                        color = if (cell == '#' || cell == 'x') color else dimColor,
                        radius = d / 2f * scale,
                        center = Offset(col * pitch + d / 2f, row * pitch + d / 2f),
                    )
                }
            }
        }
    }
}

/** `x` ドットの径の倍率。セル中心はそのままに小さく描く。 */
private const val OUTLINE_DOT_SCALE = 0.5f
