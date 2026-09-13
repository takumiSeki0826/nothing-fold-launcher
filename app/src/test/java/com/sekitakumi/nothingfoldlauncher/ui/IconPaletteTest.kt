package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class IconPaletteTest {

    @Test
    fun `maps index within range to itself`() {
        assertEquals(0, paletteIndexForAppIndex(0, paletteSize = 4))
        assertEquals(3, paletteIndexForAppIndex(3, paletteSize = 4))
    }

    @Test
    fun `wraps around when index exceeds palette size`() {
        assertEquals(0, paletteIndexForAppIndex(4, paletteSize = 4))
        assertEquals(1, paletteIndexForAppIndex(5, paletteSize = 4))
        assertEquals(2, paletteIndexForAppIndex(10, paletteSize = 4))
    }

    @Test
    fun `handles negative index safely`() {
        assertEquals(3, paletteIndexForAppIndex(-1, paletteSize = 4))
        assertEquals(0, paletteIndexForAppIndex(-4, paletteSize = 4))
    }
}
