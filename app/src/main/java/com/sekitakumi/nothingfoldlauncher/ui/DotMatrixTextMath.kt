package com.sekitakumi.nothingfoldlauncher.ui

data class DotRun(val text: String, val isDot: Boolean)

/** ドット書体の数字の高さ / fontSize の比（一般的な書体の数字の高さ比）。 */
const val DIGIT_HEIGHT_RATIO = 0.72f

private fun isDotChar(c: Char): Boolean = hasDotMatrixGlyph(c)

/** 連続する同種（ドット書体のグリフがある文字 / それ以外）の文字を1つの run にまとめる。 */
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

/** グリフ1文字の幅。列数 n のとき (n-1)×ピッチ + d（5列で 7d）。 */
fun dotMatrixGlyphWidthPx(char: Char, d: Float): Float =
    (dotMatrixGlyph(char)[0].length - 1) * d * DOT_PITCH_RATIO + d

/** 文字間のギャップはピッチ（d × [DOT_PITCH_RATIO]）1つ分に [extraGapPx]（字間）を足したもの。 */
fun dotMatrixRunWidthPx(text: String, d: Float, extraGapPx: Float = 0f): Float =
    text.sumOf { dotMatrixGlyphWidthPx(it, d).toDouble() }.toFloat() +
        (text.length - 1) * (d * DOT_PITCH_RATIO + extraGapPx)

const val DOT_PITCH_RATIO = 1.5f

private const val ELLIPSIS = "…"

/** グリフのある文字は実寸、無い文字（日本語など）は [fontSizePx] 幅として見積もる。 */
fun dotMatrixTextWidthPx(text: String, d: Float, fontSizePx: Float, extraGapPx: Float = 0f): Float {
    var total = 0f
    var count = 0
    var i = 0
    while (i < text.length) {
        val cp = text.codePointAt(i)
        total += if (cp <= 0xFFFF && hasDotMatrixGlyph(cp.toChar())) dotMatrixGlyphWidthPx(cp.toChar(), d) else fontSizePx
        count++
        i += Character.charCount(cp)
    }
    return if (count == 0) 0f else total + (count - 1) * (d * DOT_PITCH_RATIO + extraGapPx)
}

/** [maxWidthPx] に収まらないとき、末尾を「…」に置き換えて収める。「…」すら入らなければ空文字。 */
fun truncateForWidth(text: String, d: Float, fontSizePx: Float, maxWidthPx: Float, extraGapPx: Float = 0f): String {
    if (dotMatrixTextWidthPx(text, d, fontSizePx, extraGapPx) <= maxWidthPx) return text
    var n = text.codePointCount(0, text.length) - 1
    while (n >= 0) {
        val prefix = text.substring(0, text.offsetByCodePoints(0, n)) + ELLIPSIS
        if (dotMatrixTextWidthPx(prefix, d, fontSizePx, extraGapPx) <= maxWidthPx) return prefix
        n--
    }
    return ""
}
