package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

// Represents digits 0-9 on a 5x7 dot grid
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
)

@Composable
fun DotMatrixDigits(
    text: String,
    modifier: Modifier = Modifier,
    dotColor: Color = Color.White,
) {
    Canvas(modifier = modifier) {
        val dotSizePx = minOf(size.width / (text.length * 4f), size.height / 7f)
        val cellPx = dotSizePx * 1.4f
        val charGapPx = dotSizePx * 1.5f

        var xOffset = 0f
        for (ch in text) {
            val pattern = DIGIT_PATTERNS[ch] ?: List(7) { "000" }
            for (row in pattern.indices) {
                for (col in pattern[row].indices) {
                    if (pattern[row][col] == '1') {
                        drawCircle(
                            color = dotColor,
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
