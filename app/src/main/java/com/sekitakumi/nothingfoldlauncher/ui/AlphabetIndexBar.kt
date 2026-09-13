package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun AlphabetIndexBar(
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    var activeIndex by remember { mutableStateOf<Int?>(null) }
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(20.dp)
            .pointerInput(Unit) {
                val barHeight = size.height.toFloat()
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    var currentLetter = letterForBarPosition(down.position.y / barHeight)
                    activeIndex = ALPHABET_INDEX_LETTERS.indexOf(currentLetter)
                    onLetterSelected(currentLetter)
                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    var pointerId = down.id
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                        if (!change.pressed) break
                        change.consume()
                        val letter = letterForBarPosition(change.position.y / barHeight)
                        if (letter != currentLetter) {
                            currentLetter = letter
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        }
                        activeIndex = ALPHABET_INDEX_LETTERS.indexOf(letter)
                        onLetterSelected(letter)
                        pointerId = change.id
                    }
                    activeIndex = null
                }
            },
    ) {
        ALPHABET_INDEX_LETTERS.forEachIndexed { index, letter ->
            val distance = activeIndex?.let { abs(it - index) }
            val targetScale = distance?.let { alphabetIndexLetterScale(it) } ?: 1f
            val scale by animateFloatAsState(
                targetValue = targetScale,
                label = "alphabet-index-letter-scale",
            )
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = letter.toString(),
                    color = if (distance == 0) Color.White else Color.Gray,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
                )
            }
        }
    }
}
