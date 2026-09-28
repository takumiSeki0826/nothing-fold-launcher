package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingGrays
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

// Matches the 48dp IconButton touch target that sets NowPlayingWidget's header
// height, so both cards' headers (and the dot grids below them) line up.
private val HEADER_HEIGHT = 48.dp

@Composable
fun CalendarWidget(onClick: () -> Unit, onLongClick: () -> Unit = {}, modifier: Modifier = Modifier) {
    val now by produceState(initialValue = Calendar.getInstance()) {
        while (true) {
            value = Calendar.getInstance()
            delay(60_000L)
        }
    }

    Column(
        modifier = modifier
            .background(NothingGrays.Base, RoundedCornerShape(24.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp),
    ) {
        Box(modifier = Modifier.height(HEADER_HEIGHT), contentAlignment = Alignment.CenterStart) {
            Text(text = monthText(now.time), color = Color.White, fontSize = 15.sp)
        }

        MiniDotCalendar(
            calendar = now,
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .padding(top = 12.dp),
        )
    }
}

private val DOT_COLOR = NothingGrays.OnBase
private val DOT_FLASH_COLOR = Color(0xFFD1432B)

// How much of the shared flash progress (0f..1f) one dot's flash-up and
// flash-down takes, centered on its randomly assigned peak.
private const val DOT_FLASH_PULSE_WIDTH = 0.15f

@Composable
private fun MiniDotCalendar(calendar: Calendar, modifier: Modifier = Modifier) {
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val grid = calendarDotGrid(year, month, today)

    // Every non-today dot gets a random point in the timeline at which it
    // flashes to the accent color before settling back to gray, so the
    // whole grid appears to sparkle rather than lighting up all at once.
    // The peak is kept away from the 0f/1f edges so each dot's flash fully
    // ramps up and back down within the animation.
    val flashPeaks = remember(grid) {
        grid.dots.associate {
            it.day to (DOT_FLASH_PULSE_WIDTH + Random.nextFloat() * (1f - 2 * DOT_FLASH_PULSE_WIDTH))
        }
    }
    val flashProgress = remember(grid) { Animatable(0f) }
    LaunchedEffect(grid) {
        flashProgress.animateTo(1f, tween(durationMillis = 1000, easing = LinearEasing))
    }

    Canvas(modifier = modifier) {
        val cellWidth = size.width / grid.columns
        val cellHeight = size.height / grid.rows
        val dotRadius = minOf(cellWidth, cellHeight) * 0.28f

        for (dot in grid.dots) {
            val color = if (dot.isToday) {
                DOT_FLASH_COLOR
            } else {
                val intensity = dotFlashIntensity(
                    progress = flashProgress.value,
                    peak = flashPeaks.getValue(dot.day),
                    pulseWidth = DOT_FLASH_PULSE_WIDTH,
                )
                lerp(DOT_COLOR, DOT_FLASH_COLOR, intensity)
            }
            drawCircle(
                color = color,
                radius = if (dot.isToday) dotRadius * 1.4f else dotRadius,
                center = Offset(
                    x = dot.col * cellWidth + cellWidth / 2f,
                    y = dot.row * cellHeight + cellHeight / 2f,
                ),
            )
        }
    }
}

private fun monthText(date: Date): String =
    SimpleDateFormat("MMMM", Locale.ENGLISH).format(date)
