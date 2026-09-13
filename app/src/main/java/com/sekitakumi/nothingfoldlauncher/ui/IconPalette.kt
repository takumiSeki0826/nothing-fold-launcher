package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.ui.graphics.Color

fun paletteIndexForAppIndex(index: Int, paletteSize: Int): Int {
    require(paletteSize > 0)
    return ((index % paletteSize) + paletteSize) % paletteSize
}

private val ICON_PALETTE = listOf(
    Color.Black,
    Color(0xFFD1432B),
    Color.White,
    Color(0xFF8A8A8A),
)

fun colorForAppIndex(index: Int): Color =
    ICON_PALETTE[paletteIndexForAppIndex(index, ICON_PALETTE.size)]
