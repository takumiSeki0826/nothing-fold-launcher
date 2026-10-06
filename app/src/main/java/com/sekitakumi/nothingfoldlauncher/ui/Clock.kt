package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun Clock(modifier: Modifier = Modifier) {
    val now by produceState(initialValue = Date()) {
        while (true) {
            value = Date()
            delay(1_000L)
        }
    }

    Column(modifier = modifier.padding(top = 12.dp)) {
        Text(
            text = dateText(now),
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Row(verticalAlignment = Alignment.Bottom) {
            DotMatrixTime(text = timeText(now), color = Color.White)
            Text(
                text = secondsText(now),
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun DotMatrixTime(text: String, color: Color, modifier: Modifier = Modifier) {
    val dotSize = 4.dp
    val dotGap = 2.dp
    val charGap = 6.dp

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(charGap)) {
        for (char in text) {
            val glyph = dotMatrixGlyph(char)
            Column(verticalArrangement = Arrangement.spacedBy(dotGap)) {
                for (row in glyph) {
                    Row(horizontalArrangement = Arrangement.spacedBy(dotGap)) {
                        for (cell in row) {
                            val dotColor = if (cell == '#') color else Color.Transparent
                            Box(
                                modifier = Modifier
                                    .size(dotSize)
                                    .background(dotColor, shape = CircleShape),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun timeText(date: Date): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)

private fun secondsText(date: Date): String =
    SimpleDateFormat("ss", Locale.getDefault()).format(date)

private fun dateText(date: Date): String =
    SimpleDateFormat("EEE, MMM d", Locale.ENGLISH).format(date)
