package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class DotMatrixTextMathTest {

    @Test
    fun `splitDotRuns returns empty list for empty text`() {
        assertEquals(emptyList<DotRun>(), splitDotRuns(""))
    }

    @Test
    fun `splitDotRuns separates sign, digits and unit`() {
        assertEquals(
            listOf(DotRun("-67dBm", true)),
            splitDotRuns("-67dBm"),
        )
    }

    @Test
    fun `splitDotRuns separates digits and percent`() {
        assertEquals(
            listOf(DotRun("12%", true)),
            splitDotRuns("12%"),
        )
    }

    @Test
    fun `splitDotRuns keeps consecutive non-digits in one run`() {
        assertEquals(listOf(DotRun("あい", false)), splitDotRuns("あい"))
    }

    @Test
    fun `splitDotRuns handles date text`() {
        assertEquals(
            listOf(DotRun("Mon, Oct 6", true)),
            splitDotRuns("Mon, Oct 6"),
        )
    }

    @Test
    fun `splitDotRuns draws degree sign in dots`() {
        assertEquals(listOf(DotRun("23°", true)), splitDotRuns("23°"))
    }

    @Test
    fun `splitDotRuns draws decimal point and comma in dots`() {
        assertEquals(listOf(DotRun("1.5,", true)), splitDotRuns("1.5,"))
    }

    @Test
    fun `splitDotRuns draws unit with slash in dots`() {
        assertEquals(listOf(DotRun("0KB/s", true)), splitDotRuns("0KB/s"))
    }

    @Test
    fun `splitDotRuns falls back to text only for chars without glyph`() {
        assertEquals(
            listOf(DotRun("A", true), DotRun("あ", false), DotRun("1", true)),
            splitDotRuns("Aあ1"),
        )
    }

    @Test
    fun `dotMatrixRunWidthPx sums glyph widths and gaps`() {
        // 数字は 7d、ギャップは 1.5d、空白は 2 列 = 2.5d
        assertEquals(7f + 1.5f + 7f, dotMatrixRunWidthPx("12", 1f), 0.001f)
        assertEquals(7f + 1.5f + 2.5f + 1.5f + 7f, dotMatrixRunWidthPx("1 2", 1f), 0.001f)
    }

    @Test
    fun `dotMatrixRunWidthPx adds extra gap between chars`() {
        assertEquals(7f + 1.5f + 2f + 7f, dotMatrixRunWidthPx("12", 1f, extraGapPx = 2f), 0.001f)
        assertEquals(7f, dotMatrixRunWidthPx("1", 1f, extraGapPx = 2f), 0.001f)
    }

    @Test
    fun `dotMatrixDotSizePx scales font size by digit height ratio`() {
        assertEquals(7.2f, dotMatrixDotSizePx(100f), 0.001f)
    }

    @Test
    fun `dotMatrixTextWidthPx measures glyph chars by glyph and others by font size`() {
        // 'A' は 7d、ギャップは 1.5d
        assertEquals(7f + 1.5f + 7f, dotMatrixTextWidthPx("AB", d = 1f, fontSizePx = 10f), 0.001f)
        // 日本語は fontSize 幅として数える
        assertEquals(10f + 1.5f + 10f, dotMatrixTextWidthPx("あい", d = 1f, fontSizePx = 10f), 0.001f)
        assertEquals(0f, dotMatrixTextWidthPx("", d = 1f, fontSizePx = 10f), 0.001f)
    }

    @Test
    fun `truncateForWidth keeps text that fits`() {
        assertEquals("HELLO", truncateForWidth("HELLO", d = 1f, fontSizePx = 10f, maxWidthPx = 100f))
    }

    @Test
    fun `truncateForWidth cuts text and appends ellipsis`() {
        // "…" = 7、n 文字 + "…" = (n+1)*7 + n*1.5 <= 30 → n = 2
        assertEquals("HE…", truncateForWidth("HELLO", d = 1f, fontSizePx = 10f, maxWidthPx = 30f))
    }

    @Test
    fun `truncateForWidth returns empty when not even the ellipsis fits`() {
        assertEquals("", truncateForWidth("HELLO", d = 1f, fontSizePx = 10f, maxWidthPx = 5f))
    }

    @Test
    fun `truncateForWidth accounts for extra gap`() {
        // extraGap 2 → 文字間 3.5。"…" 単独 7、n=1: 7+3.5+7=17.5 <= 18
        assertEquals("H…", truncateForWidth("HELLO", d = 1f, fontSizePx = 10f, maxWidthPx = 18f, extraGapPx = 2f))
    }

    @Test
    fun `truncateForWidth does not split a surrogate pair`() {
        val face = "\uD83D\uDE00"
        // 絵文字は fontSize 幅 10 として数える: 10+1.5+7 = 18.5 <= 25 < 10+1.5+10+1.5+7
        assertEquals("$face…", truncateForWidth(face + face + face, d = 1f, fontSizePx = 10f, maxWidthPx = 25f))
    }
}
