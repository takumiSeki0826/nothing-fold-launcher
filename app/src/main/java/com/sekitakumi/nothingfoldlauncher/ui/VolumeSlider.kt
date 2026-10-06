package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun VolumeSlider(
    ratio: Float,
    onRatioChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    trackHeight: Dp = DEFAULT_FADER_TRACK_HEIGHT,
) {
    FaderSlider(
        label = "Audio",
        ratio = ratio,
        onRatioChange = onRatioChange,
        modifier = modifier,
        trackHeight = trackHeight,
    )
}
