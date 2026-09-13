package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun VolumeSlider(
    ratio: Float,
    onRatioChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    FaderSlider(label = "Audio", ratio = ratio, onRatioChange = onRatioChange, modifier = modifier)
}
