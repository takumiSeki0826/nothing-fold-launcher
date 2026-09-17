package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.ui.graphics.Color

enum class IconPaletteColor { ACCENT, BLACK, WHITE, GRAY }

fun colorValue(paletteColor: IconPaletteColor): Color = when (paletteColor) {
    IconPaletteColor.ACCENT -> Color(0xFFD1432B)
    IconPaletteColor.BLACK -> Color.Black
    IconPaletteColor.WHITE -> Color.White
    IconPaletteColor.GRAY -> Color(0xFF8A8A8A)
}

fun iconBackgroundColor(paletteColor: IconPaletteColor?): Color = colorValue(paletteColor ?: IconPaletteColor.GRAY)

fun iconBorderColor(paletteColor: IconPaletteColor?): Color? =
    if ((paletteColor ?: IconPaletteColor.GRAY) == IconPaletteColor.BLACK) {
        Color.White.copy(alpha = 0.28f)
    } else {
        null
    }
