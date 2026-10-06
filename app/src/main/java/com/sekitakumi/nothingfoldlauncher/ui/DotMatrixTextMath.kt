package com.sekitakumi.nothingfoldlauncher.ui

data class DotRun(val text: String, val isDot: Boolean)

/** ドット書体の数字の高さ / fontSize の比（一般的な書体の数字の高さ比）。 */
const val DIGIT_HEIGHT_RATIO = 0.72f

private fun isDotChar(c: Char): Boolean = c in '0'..'9'

/** 連続する同種（半角数字 / それ以外）の文字を1つの run にまとめる。 */
fun splitDotRuns(text: String): List<DotRun> {
    if (text.isEmpty()) return emptyList()
    val runs = mutableListOf<DotRun>()
    var start = 0
    for (i in 1..text.length) {
        if (i == text.length || isDotChar(text[i]) != isDotChar(text[start])) {
            runs += DotRun(text.substring(start, i), isDotChar(text[start]))
            start = i
        }
    }
    return runs
}

/** 5×7グリフの高さ = 7d + 6×(d/2) = 10d なので、d = 数字の高さ / 10。 */
fun dotMatrixDotSizePx(fontSizePx: Float): Float = fontSizePx * DIGIT_HEIGHT_RATIO / 10f
