package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingGrays
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val GRID_COLUMNS = 8
private const val GRID_ROWS = 5

private val DOT_GRID_HEIGHT = 100.dp

// Expanded (large screen) layout: card size stays the same; only the contents are inset and shrunk.
private val COMPACT_CONTENT_INSET = 48.dp
private val ARTIST_OFFSET_Y = 42.dp
private val PLAY_BUTTON_OFFSET_X = 8.dp
private val PLAY_BUTTON_OFFSET_Y = 4.dp

@Composable
fun NowPlayingWidget(
    nowPlaying: NowPlayingState?,
    permissionGranted: Boolean,
    onTogglePlayPause: () -> Unit,
    onRequestPermission: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val isPlaying = nowPlaying?.isPlaying == true

    val levels by produceState(initialValue = FloatArray(GRID_COLUMNS), isPlaying) {
        while (true) {
            value = if (isPlaying) {
                FloatArray(GRID_COLUMNS) { Random.nextFloat() }
            } else {
                FloatArray(GRID_COLUMNS) { 0.12f }
            }
            delay(280L)
        }
    }

    Column(
        modifier = modifier
            .background(NothingGrays.Base, RoundedCornerShape(24.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = if (compact) COMPACT_CONTENT_INSET else 24.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = NOW_PLAYING_HEADER_MIN_HEIGHT_DP.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (permissionGranted) {
                // Title sits at the same height as CalendarWidget's header text. The artist
                // hangs below it without adding to the header height, so both cards stay equal.
                Box(
                    modifier = Modifier.weight(1f).height(NOW_PLAYING_HEADER_MIN_HEIGHT_DP.dp),
                ) {
                    DotMatrixText(
                        text = nowPlaying?.title?.let(::nowPlayingDisplayText) ?: "Not Playing",
                        color = Color.White,
                        fontSize = 13.sp,
                        ellipsize = true,
                        modifier = Modifier.align(Alignment.CenterStart),
                    )
                    if (nowPlaying?.artist != null) {
                        DotMatrixText(
                            text = nowPlayingDisplayText(nowPlaying.artist),
                            color = Color.Gray,
                            fontSize = 11.sp,
                            ellipsize = true,
                            modifier = Modifier.align(Alignment.TopStart).offset(y = ARTIST_OFFSET_Y),
                        )
                    }
                }
                if (nowPlaying != null) {
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier.offset(x = PLAY_BUTTON_OFFSET_X, y = PLAY_BUTTON_OFFSET_Y),
                    ) {
                        DotMatrixIcon(
                            rows = if (nowPlaying.isPlaying) DotIcons.PAUSE else DotIcons.PLAY,
                            color = Color.White,
                            dotSize = 2.dp,
                        )
                    }
                }
            } else {
                DotMatrixText(
                    text = "Now Playing",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onRequestPermission) {
                    DotMatrixIcon(rows = DotIcons.BELL, color = Color.Gray, dotSize = 2.dp)
                }
            }
        }

        DotGrid(
            columns = GRID_COLUMNS,
            rows = GRID_ROWS,
            levels = levels,
            modifier = Modifier
                .fillMaxWidth()
                .height(DOT_GRID_HEIGHT)
                .padding(top = 18.dp, bottom = 8.dp),
            dotRadius = if (compact) COMPACT_DOT_RADIUS else DOT_RADIUS,
            flashOnEnter = nowPlaying?.isPlaying != true,
        )
    }
}

@Composable
private fun DotGrid(
    columns: Int,
    rows: Int,
    levels: FloatArray,
    dotRadius: Dp,
    modifier: Modifier = Modifier,
    flashOnEnter: Boolean = true,
) {
    // Same sparkle as the calendar grid: each dot flashes to the accent color
    // once at a random point of a 1s timeline whenever the grid enters composition.
    // Skipped while music plays: the equalizer already lights the dots.
    val flashPeaks = remember(columns, rows) {
        FloatArray(columns * rows) {
            DOT_FLASH_PULSE_WIDTH + Random.nextFloat() * (1f - 2 * DOT_FLASH_PULSE_WIDTH)
        }
    }
    val flashProgress = remember(columns, rows) { Animatable(0f) }
    LaunchedEffect(columns, rows) {
        flashProgress.animateTo(1f, tween(durationMillis = 1000, easing = LinearEasing))
    }

    Canvas(modifier = modifier) {
        val cellWidth = size.width / columns
        val cellHeight = size.height / rows
        val dotRadius = dotRadius.toPx()

        for (col in 0 until columns) {
            val level = levels.getOrElse(col) { 0f }.coerceIn(0f, 1f)
            val litRows = (level * rows).toInt().coerceIn(0, rows)

            for (row in 0 until rows) {
                val litFromBottom = row >= rows - litRows
                val color = if (litFromBottom) {
                    DOT_FLASH_COLOR
                } else {
                    val intensity = if (flashOnEnter) {
                        dotFlashIntensity(
                            progress = flashProgress.value,
                            peak = flashPeaks[row * columns + col],
                            pulseWidth = DOT_FLASH_PULSE_WIDTH,
                        )
                    } else {
                        0f
                    }
                    lerp(NothingGrays.OnBase, DOT_FLASH_COLOR, intensity)
                }
                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(
                        x = dotRadius + col * (size.width - 2 * dotRadius) / (columns - 1),
                        y = row * cellHeight + cellHeight / 2f,
                    ),
                )
            }
        }
    }
}
