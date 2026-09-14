package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val GRID_COLUMNS = 7

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
            .background(Color(0xFF111111), RoundedCornerShape(24.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp),
    ) {
        Text(text = monthYearText(now.time), color = Color.White, fontSize = 15.sp)

        MiniDotCalendar(
            calendar = now,
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .padding(top = 12.dp),
        )
    }
}

@Composable
private fun MiniDotCalendar(calendar: Calendar, modifier: Modifier = Modifier) {
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val today = calendar.get(Calendar.DAY_OF_MONTH)

    val totalDays = daysInMonth(year, month)
    val offset = firstWeekdayOffset(year, month)
    val usedRows = ((offset + totalDays - 1) / GRID_COLUMNS) + 1

    Canvas(modifier = modifier) {
        val cellWidth = size.width / GRID_COLUMNS
        val cellHeight = size.height / usedRows
        val dotRadius = minOf(cellWidth, cellHeight) * 0.28f

        for (day in 1..totalDays) {
            val cellIndex = offset + day - 1
            val row = cellIndex / GRID_COLUMNS
            val col = cellIndex % GRID_COLUMNS
            val isToday = day == today
            drawCircle(
                color = if (isToday) Color(0xFFD1432B) else Color(0xFF4A4A4A),
                radius = if (isToday) dotRadius * 1.4f else dotRadius,
                center = Offset(
                    x = col * cellWidth + cellWidth / 2f,
                    y = row * cellHeight + cellHeight / 2f,
                ),
            )
        }
    }
}

private fun monthYearText(date: Date): String =
    SimpleDateFormat("yyyy MMMM", Locale.ENGLISH).format(date)
