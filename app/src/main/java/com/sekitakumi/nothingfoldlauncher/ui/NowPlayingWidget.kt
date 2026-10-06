package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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

private const val GRID_COLUMNS = 9
private const val GRID_ROWS = 5

@Composable
fun NowPlayingWidget(
    nowPlaying: NowPlayingState?,
    permissionGranted: Boolean,
    onTogglePlayPause: () -> Unit,
    onRequestPermission: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
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
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = NOW_PLAYING_HEADER_MIN_HEIGHT_DP.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (permissionGranted) {
                Column(modifier = Modifier.weight(1f)) {
                    DotMatrixText(
                        text = nowPlaying?.title?.let(::nowPlayingDisplayText) ?: "Not Playing",
                        color = Color.White,
                        fontSize = 13.sp,
                        ellipsize = true,
                    )
                    if (nowPlaying?.artist != null) {
                        DotMatrixText(
                            text = nowPlayingDisplayText(nowPlaying.artist),
                            color = Color.Gray,
                            fontSize = 11.sp,
                            ellipsize = true,
                            modifier = Modifier.padding(top = 9.dp),
                        )
                    }
                }
                if (nowPlaying != null) {
                    IconButton(onClick = onTogglePlayPause) {
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
                .height(90.dp)
                .padding(top = 12.dp),
        )
    }
}

@Composable
private fun DotGrid(
    columns: Int,
    rows: Int,
    levels: FloatArray,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val cellWidth = size.width / columns
        val cellHeight = size.height / rows
        val dotRadius = minOf(cellWidth, cellHeight) * 0.28f

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
                        x = col * cellWidth + cellWidth / 2f,
                        y = row * cellHeight + cellHeight / 2f,
                    ),
                )
            }
        }
    }
}
