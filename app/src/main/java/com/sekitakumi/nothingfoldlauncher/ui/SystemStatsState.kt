package com.sekitakumi.nothingfoldlauncher.ui

/** Individual fields are null until their first sample is available (CPU/network need two samples for a delta). */
data class SystemStatsState(
    val cpuPercent: Int? = null,
    val memoryPercent: Int? = null,
    val storagePercent: Int? = null,
    val uploadBytesPerSecond: Long? = null,
    val downloadBytesPerSecond: Long? = null,
    val signalDbm: Int? = null,
    val signalIsWifi: Boolean = false,
    val dailyMobileDataUsageBytes: Long? = null,
)
