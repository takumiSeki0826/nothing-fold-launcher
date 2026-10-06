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
            listOf(DotRun("-", false), DotRun("67", true), DotRun("dBm", false)),
            splitDotRuns("-67dBm"),
        )
    }

    @Test
    fun `splitDotRuns separates digits and percent`() {
        assertEquals(
            listOf(DotRun("12", true), DotRun("%", false)),
            splitDotRuns("12%"),
        )
    }

    @Test
    fun `splitDotRuns keeps consecutive non-digits in one run`() {
        assertEquals(listOf(DotRun("--", false)), splitDotRuns("--"))
    }

    @Test
    fun `splitDotRuns handles date text`() {
        assertEquals(
            listOf(DotRun("Mon, Oct ", false), DotRun("6", true)),
            splitDotRuns("Mon, Oct 6"),
        )
    }

    @Test
    fun `dotMatrixDotSizePx scales font size by digit height ratio`() {
        assertEquals(7.2f, dotMatrixDotSizePx(100f), 0.001f)
    }
}
