package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class IconPaletteTest {

    @Test
    fun `maps each palette color to its expected compose color`() {
        assertEquals(Color(0xFFD1432B), colorValue(IconPaletteColor.ACCENT))
        assertEquals(Color.Black, colorValue(IconPaletteColor.BLACK))
        assertEquals(Color.White, colorValue(IconPaletteColor.WHITE))
        assertEquals(Color(0xFF8A8A8A), colorValue(IconPaletteColor.GRAY))
    }

    @Test
    fun `icon background falls back to gray when no color is assigned`() {
        assertEquals(Color(0xFF8A8A8A), iconBackgroundColor(null))
    }

    @Test
    fun `icon background uses the assigned color when present`() {
        assertEquals(Color.White, iconBackgroundColor(IconPaletteColor.WHITE))
        assertEquals(Color(0xFF8A8A8A), iconBackgroundColor(IconPaletteColor.GRAY))
    }

    @Test
    fun `black tile gets a faint border so it doesn't blend into a dark wallpaper`() {
        assertEquals(Color.White.copy(alpha = 0.28f), iconBorderColor(IconPaletteColor.BLACK))
    }

    @Test
    fun `non-black tiles have no border`() {
        assertEquals(null, iconBorderColor(IconPaletteColor.WHITE))
        assertEquals(null, iconBorderColor(IconPaletteColor.GRAY))
        assertEquals(null, iconBorderColor(IconPaletteColor.ACCENT))
        assertEquals(null, iconBorderColor(null))
    }
}
