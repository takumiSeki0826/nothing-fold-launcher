package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingGrays

private const val GAUGE_SEGMENT_COUNT = 5
private val CRITICAL_COLOR = Color(0xFFD1432B)

@Composable
fun SystemStatsWidget(stats: SystemStatsState, onNetClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        PercentStatRow(label = "CPU", percent = stats.cpuPercent)
        PercentStatRow(label = "MEM", percent = stats.memoryPercent)
        PercentStatRow(label = "STO", percent = stats.storagePercent)
        SignalStatRow(dbm = stats.signalDbm, isWifi = stats.signalIsWifi)
        DataUsageStatRow(usageBytes = stats.dailyMobileDataUsageBytes)
        Column(
            modifier = Modifier.clickable(onClick = onNetClick),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            ThroughputStatRow(label = "NET ↑", bytesPerSecond = stats.uploadBytesPerSecond)
            ThroughputStatRow(label = "NET ↓", bytesPerSecond = stats.downloadBytesPerSecond)
        }
    }
}

@Composable
private fun PercentStatRow(label: String, percent: Int?, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, color = Color.Gray, fontSize = 10.sp)
        Spacer(modifier = Modifier.width(6.dp))
        if (percent != null) {
            DotGauge(
                filledSegments = gaugeFilledSegments(percent, GAUGE_SEGMENT_COUNT),
                isCritical = isCriticalUsage(percent),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "$percent%", color = Color.White, fontSize = 12.sp)
        } else {
            Text(text = "--", color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ThroughputStatRow(label: String, bytesPerSecond: Long?, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, color = Color.Gray, fontSize = 10.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = bytesPerSecond?.let(::formatThroughput) ?: "--",
            color = Color.White,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun SignalStatRow(dbm: Int?, isWifi: Boolean, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = "SIG", color = Color.Gray, fontSize = 10.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = dbm?.let { "${it}dBm" } ?: "--",
            color = if (dbm != null && isWeakSignal(dbm, isWifi)) CRITICAL_COLOR else Color.White,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun DataUsageStatRow(usageBytes: Long?, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text = "DATA", color = Color.Gray, fontSize = 10.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = usageBytes?.let(::formatDataUsage) ?: "--",
            color = Color.White,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun DotGauge(filledSegments: Int, isCritical: Boolean, modifier: Modifier = Modifier) {
    val filledColor = if (isCritical) CRITICAL_COLOR else Color.White
    Canvas(modifier = modifier.width(30.dp).height(8.dp)) {
        val dotRadius = size.height / 2f
        val gap = (size.width - dotRadius * 2 * GAUGE_SEGMENT_COUNT) / (GAUGE_SEGMENT_COUNT - 1)
        for (i in 0 until GAUGE_SEGMENT_COUNT) {
            val cx = dotRadius + i * (dotRadius * 2 + gap)
            drawCircle(
                color = if (i < filledSegments) filledColor else NothingGrays.Base,
                radius = dotRadius,
                center = Offset(cx, size.height / 2f),
            )
        }
    }
}
