package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 5x7ドットグリッドで0-9とコロンを表現する（Nothing公式フォントはライセンス上使えないため自前描画）
private val DIGIT_PATTERNS: Map<Char, List<String>> = mapOf(
    '0' to listOf("111", "101", "101", "101", "101", "101", "111"),
    '1' to listOf("010", "110", "010", "010", "010", "010", "111"),
    '2' to listOf("111", "001", "001", "111", "100", "100", "111"),
    '3' to listOf("111", "001", "001", "111", "001", "001", "111"),
    '4' to listOf("101", "101", "101", "111", "001", "001", "001"),
    '5' to listOf("111", "100", "100", "111", "001", "001", "111"),
    '6' to listOf("111", "100", "100", "111", "101", "101", "111"),
    '7' to listOf("111", "001", "001", "010", "010", "010", "010"),
    '8' to listOf("111", "101", "101", "111", "101", "101", "111"),
    '9' to listOf("111", "101", "101", "111", "001", "001", "111"),
    ':' to listOf("000", "010", "000", "000", "000", "010", "000"),
)

@Composable
fun DotMatrixClock(modifier: Modifier = Modifier) {
    val timeText by produceState(initialValue = currentTimeText()) {
        while (true) {
            value = currentTimeText()
            delay(1_000L)
        }
    }

    val dotSize = 6.dp
    val dotGap = 3.dp
    val charGap = dotGap * 3

    Canvas(
        modifier = modifier
            .height(dotSize * 7 + dotGap * 6)
            .width((dotSize * 3 + dotGap * 2 + charGap) * timeText.length),
    ) {
        val dotSizePx = dotSize.toPx()
        val dotGapPx = dotGap.toPx()
        val charGapPx = charGap.toPx()
        val cellPx = dotSizePx + dotGapPx

        var xOffset = 0f
        for (ch in timeText) {
            val pattern = DIGIT_PATTERNS[ch] ?: List(7) { "000" }
            for (row in pattern.indices) {
                for (col in pattern[row].indices) {
                    if (pattern[row][col] == '1') {
                        drawCircle(
                            color = Color.White,
                            radius = dotSizePx / 2,
                            center = Offset(
                                x = xOffset + col * cellPx + dotSizePx / 2,
                                y = row * cellPx + dotSizePx / 2,
                            ),
                        )
                    }
                }
            }
            xOffset += cellPx * 3 + charGapPx
        }
    }
}

private fun currentTimeText(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
