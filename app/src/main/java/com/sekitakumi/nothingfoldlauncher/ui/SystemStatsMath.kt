package com.sekitakumi.nothingfoldlauncher.ui

import kotlin.math.roundToInt

fun usedPercent(totalBytes: Long, freeBytes: Long): Int {
    if (totalBytes <= 0) return 0
    return (((totalBytes - freeBytes) * 100.0) / totalBytes).roundToInt().coerceIn(0, 100)
}

fun gaugeFilledSegments(percent: Int, segmentCount: Int): Int =
    ((percent / 100.0) * segmentCount).roundToInt().coerceIn(0, segmentCount)

fun isCriticalUsage(percent: Int): Boolean = percent >= 90

fun bytesPerSecond(deltaBytes: Long, deltaMillis: Long): Long {
    if (deltaMillis <= 0) return 0L
    return deltaBytes * 1000 / deltaMillis
}

private const val BYTES_PER_MB = 1024.0 * 1024.0

fun formatThroughput(bytesPerSecond: Long): String =
    if (bytesPerSecond < BYTES_PER_MB) {
        "${(bytesPerSecond / 1024.0).roundToInt()}KB/s"
    } else {
        "%.1fMB/s".format(bytesPerSecond / BYTES_PER_MB)
    }

data class CpuTimes(val idle: Long, val total: Long)

/** Parses the aggregate "cpu  ..." line from /proc/stat; returns null for per-core lines or garbage. */
fun parseCpuTimes(statLine: String): CpuTimes? {
    val parts = statLine.trim().split(Regex("\\s+"))
    if (parts.firstOrNull() != "cpu") return null
    val values = parts.drop(1).mapNotNull { it.toLongOrNull() }
    if (values.size < 4) return null
    val idle = values[3] + values.getOrElse(4) { 0L }
    return CpuTimes(idle = idle, total = values.sum())
}

fun cpuUsagePercent(previous: CpuTimes, current: CpuTimes): Int {
    val totalDelta = current.total - previous.total
    if (totalDelta <= 0) return 0
    val idleDelta = current.idle - previous.idle
    return (((totalDelta - idleDelta) * 100.0) / totalDelta).roundToInt().coerceIn(0, 100)
}
