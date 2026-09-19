package com.sekitakumi.nothingfoldlauncher.ui

import android.app.ActivityManager
import android.content.Context
import android.net.TrafficStats
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.util.Log
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "SystemStatsController"
private const val POLL_INTERVAL_MILLIS = 2_000L

class SystemStatsController(private val context: Context) {

    private val _stats = MutableStateFlow(SystemStatsState())
    val stats: StateFlow<SystemStatsState> = _stats.asStateFlow()

    private var scope: CoroutineScope? = null

    // CPU% and network speed are derived from deltas, so they need a remembered previous sample.
    private var previousCpuTimes: CpuTimes? = null
    private var previousNetworkSampleMillis: Long? = null
    private var previousRxBytes: Long? = null
    private var previousTxBytes: Long? = null

    fun register() {
        val activeScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        scope = activeScope
        activeScope.launch {
            while (true) {
                _stats.value = withContext(Dispatchers.IO) { poll() }
                delay(POLL_INTERVAL_MILLIS)
            }
        }
    }

    fun unregister() {
        scope?.cancel()
        scope = null
        previousCpuTimes = null
        previousNetworkSampleMillis = null
        previousRxBytes = null
        previousTxBytes = null
    }

    private fun poll(): SystemStatsState {
        val (upload, download) = pollNetworkBytesPerSecond()
        return SystemStatsState(
            cpuPercent = pollCpuPercent(),
            memoryPercent = pollMemoryPercent(),
            storagePercent = pollStoragePercent(),
            uploadBytesPerSecond = upload,
            downloadBytesPerSecond = download,
        )
    }

    private fun pollCpuPercent(): Int? {
        val line = try {
            File("/proc/stat").useLines { it.firstOrNull() }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read /proc/stat", e)
            null
        } ?: return null

        val current = parseCpuTimes(line) ?: return null
        val previous = previousCpuTimes
        previousCpuTimes = current
        return previous?.let { cpuUsagePercent(it, current) }
    }

    private fun pollMemoryPercent(): Int? {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return null
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        if (info.totalMem <= 0) return null
        return usedPercent(totalBytes = info.totalMem, freeBytes = info.availMem)
    }

    private fun pollStoragePercent(): Int? = try {
        val statFs = StatFs(Environment.getDataDirectory().path)
        val totalBytes = statFs.blockCountLong * statFs.blockSizeLong
        val freeBytes = statFs.availableBlocksLong * statFs.blockSizeLong
        usedPercent(totalBytes = totalBytes, freeBytes = freeBytes)
    } catch (e: Exception) {
        Log.w(TAG, "Failed to read storage stats", e)
        null
    }

    private fun pollNetworkBytesPerSecond(): Pair<Long?, Long?> {
        val rxBytes = TrafficStats.getTotalRxBytes()
        val txBytes = TrafficStats.getTotalTxBytes()
        val nowMillis = SystemClock.elapsedRealtime()

        if (rxBytes == TrafficStats.UNSUPPORTED.toLong() || txBytes == TrafficStats.UNSUPPORTED.toLong()) {
            return null to null
        }

        val previousRx = previousRxBytes
        val previousTx = previousTxBytes
        val previousMillis = previousNetworkSampleMillis

        previousRxBytes = rxBytes
        previousTxBytes = txBytes
        previousNetworkSampleMillis = nowMillis

        if (previousRx == null || previousTx == null || previousMillis == null) return null to null

        val deltaMillis = nowMillis - previousMillis
        val download = bytesPerSecond(deltaBytes = rxBytes - previousRx, deltaMillis = deltaMillis)
        val upload = bytesPerSecond(deltaBytes = txBytes - previousTx, deltaMillis = deltaMillis)
        return upload to download
    }
}
