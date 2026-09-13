package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = timeText(now),
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Light,
            )
            Text(
                text = secondsText(now),
                color = Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
            )
        }
        Text(
            text = dateText(now),
            color = Color.Gray,
            fontSize = 14.sp,
        )
    }
}

private fun timeText(date: Date): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)

private fun secondsText(date: Date): String =
    SimpleDateFormat("ss", Locale.getDefault()).format(date)

private fun dateText(date: Date): String =
    SimpleDateFormat("MM.dd EEE", Locale.getDefault()).format(date)
