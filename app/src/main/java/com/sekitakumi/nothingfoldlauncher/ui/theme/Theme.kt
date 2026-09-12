package com.sekitakumi.nothingfoldlauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NothingBlack = Color(0xFF000000)
private val NothingWhite = Color(0xFFF5F5F5)
private val NothingRed = Color(0xFFD1432B)
private val NothingGray = Color(0xFF8A8A8A)

private val NothingColorScheme = darkColorScheme(
    background = NothingBlack,
    surface = NothingBlack,
    primary = NothingWhite,
    onPrimary = NothingBlack,
    onBackground = NothingWhite,
    onSurface = NothingWhite,
    secondary = NothingGray,
    tertiary = NothingRed,
)

@Composable
fun NothingFoldLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NothingColorScheme,
        typography = NothingTypography,
        content = content,
    )
}
