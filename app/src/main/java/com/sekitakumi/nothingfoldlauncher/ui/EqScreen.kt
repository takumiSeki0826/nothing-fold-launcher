package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

private val CHANNEL_SPACING = 16.dp
private val KNOB_SPACING = 24.dp
private const val KNOB_BASE_ANGLE_DEG = 0f

@Composable
fun EqScreen(
    knobNames: Map<KnobSlot, String>,
    onKnobTap: (KnobSlot) -> Unit,
    onKnobLongPress: (KnobSlot) -> Unit,
    onBackgroundLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onBackgroundLongPress() })
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(CHANNEL_SPACING)) {
            EqChannel(
                slots = listOf(KnobSlot.LEFT_MID, KnobSlot.LEFT_LOW, KnobSlot.LEFT_CFX),
                knobNames = knobNames,
                onKnobTap = onKnobTap,
                onKnobLongPress = onKnobLongPress,
                entranceOrderOffset = 0,
            )
            EqChannel(
                slots = listOf(KnobSlot.RIGHT_MID, KnobSlot.RIGHT_LOW, KnobSlot.RIGHT_CFX),
                knobNames = knobNames,
                onKnobTap = onKnobTap,
                onKnobLongPress = onKnobLongPress,
                entranceOrderOffset = 3,
            )
        }
    }
}

@Composable
private fun EqChannel(
    slots: List<KnobSlot>,
    knobNames: Map<KnobSlot, String>,
    onKnobTap: (KnobSlot) -> Unit,
    onKnobLongPress: (KnobSlot) -> Unit,
    entranceOrderOffset: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(KNOB_SPACING)) {
        slots.forEachIndexed { index, slot ->
            RotaryKnob(
                angleDeg = KNOB_BASE_ANGLE_DEG,
                label = knobNames[slot] ?: slot.defaultLabel,
                onTap = { onKnobTap(slot) },
                onLongPress = { onKnobLongPress(slot) },
                entranceWobbleDelayMs = entranceWobbleDelayMs(entranceOrderOffset + index),
            )
        }
    }
}
