package com.sekitakumi.nothingfoldlauncher.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
private const val DOT_RADIUS_RATIO = 0.22f
private const val COMPACT_DOT_RADIUS_RATIO = 0.3f
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
                .padding(top = 12.dp, bottom = 8.dp),
            dotRadiusRatio = if (compact) COMPACT_DOT_RADIUS_RATIO else DOT_RADIUS_RATIO,
        )
    }
}

@Composable
private fun DotGrid(
    columns: Int,
    rows: Int,
    levels: FloatArray,
    dotRadiusRatio: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val cellWidth = size.width / columns
        val cellHeight = size.height / rows
        val dotRadius = minOf(cellWidth, cellHeight) * dotRadiusRatio

        for (col in 0 until columns) {
            val level = levels.getOrElse(col) { 0f }.coerceIn(0f, 1f)
            val litRows = (level * rows).toInt().coerceIn(0, rows)

            for (row in 0 until rows) {
                val litFromBottom = row >= rows - litRows
                val color = if (litFromBottom) Color(0xFFD1432B) else NothingGrays.OnBase
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
