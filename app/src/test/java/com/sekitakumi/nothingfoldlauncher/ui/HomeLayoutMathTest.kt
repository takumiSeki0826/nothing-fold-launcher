package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeLayoutMathTest {

    @Test
    fun `narrow width is not expanded`() {
        assertFalse(isExpandedWidth(widthDp = 360))
        assertFalse(isExpandedWidth(widthDp = 599))
    }

    @Test
    fun `width at or above 600dp is expanded`() {
        assertTrue(isExpandedWidth(widthDp = 600))
        assertTrue(isExpandedWidth(widthDp = 1800))
    }

    @Test
    fun `grid uses 4 columns when compact`() {
        assertEquals(4, homeGridColumns(isExpanded = false))
    }

    @Test
    fun `grid uses 8 columns when expanded`() {
        assertEquals(8, homeGridColumns(isExpanded = true))
    }
}
