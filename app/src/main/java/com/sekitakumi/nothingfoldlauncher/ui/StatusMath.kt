package com.sekitakumi.nothingfoldlauncher.ui

import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager

fun batteryPercent(level: Int, scale: Int): Int {
    if (scale <= 0) return 0
    return ((level * 100) / scale).coerceIn(0, 100)
}

fun signalBars(level: Int): Int = level.coerceIn(0, 4)

// 4 bars + 3 half-width gaps = 5.5 bar-widths; dividing by 4.5 would overflow the canvas.
fun signalBarWidth(canvasWidth: Float): Float = canvasWidth / 5.5f

private val NETWORK_TYPE_3G = setOf(
    TelephonyManager.NETWORK_TYPE_UMTS,
    TelephonyManager.NETWORK_TYPE_HSDPA,
    TelephonyManager.NETWORK_TYPE_HSUPA,
    TelephonyManager.NETWORK_TYPE_HSPA,
    TelephonyManager.NETWORK_TYPE_HSPAP,
)

fun networkTypeLabel(networkType: Int, overrideNetworkType: Int): String? = when {
    overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED -> "5G+"
    overrideNetworkType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA -> "5G"
    networkType == TelephonyManager.NETWORK_TYPE_NR -> "5G"
    networkType == TelephonyManager.NETWORK_TYPE_LTE -> "4G"
    networkType in NETWORK_TYPE_3G -> "3G"
    else -> null
}

// Tailscale always assigns its tunnel interface an IPv4 address from the 100.64.0.0/10 CGNAT block.
fun isTailscaleCgnatAddress(address: ByteArray): Boolean =
    address.size == 4 && (address[0].toInt() and 0xFF) == 100 && (address[1].toInt() and 0xFF) in 64..127

enum class VpnBadge { NONE, VPN, TAILSCALE }

// Unconfirmed Tailscale detection falls back to the generic VPN badge, never to nothing.
fun vpnBadgeFor(vpnConnected: Boolean, tailscaleConnected: Boolean): VpnBadge = when {
    tailscaleConnected -> VpnBadge.TAILSCALE
    vpnConnected -> VpnBadge.VPN
    else -> VpnBadge.NONE
}

private const val WEAK_CELLULAR_DBM = -110
private const val WEAK_WIFI_DBM = -80

fun isWeakSignal(dbm: Int, isWifi: Boolean): Boolean =
    dbm <= if (isWifi) WEAK_WIFI_DBM else WEAK_CELLULAR_DBM

fun preferredSignalDbm(wifiConnected: Boolean, wifiRssi: Int?, cellularDbm: Int?): Int? =
    if (wifiConnected) wifiRssi ?: cellularDbm else cellularDbm

data class DailyUsageResult(val usageBytes: Long, val newBaselineBytes: Long, val newBaselineDate: String)

// A lower current total than the stored baseline means the device rebooted (TrafficStats'
// counters restart at boot), so bytes used between midnight and the reboot are uncounted.
fun dailyMobileUsage(
    currentTotalBytes: Long,
    baselineBytes: Long?,
    baselineDate: String?,
    today: String,
): DailyUsageResult = when {
    baselineDate != today -> DailyUsageResult(0L, currentTotalBytes, today)
    baselineBytes == null || currentTotalBytes < baselineBytes ->
        DailyUsageResult(currentTotalBytes, 0L, today)
    else -> DailyUsageResult(currentTotalBytes - baselineBytes, baselineBytes, today)
}

private const val BYTES_PER_MB = 1_000_000.0
private const val BYTES_PER_GB = 1_000_000_000L

fun formatDataUsage(bytes: Long): String =
    if (bytes >= BYTES_PER_GB) {
        "%.1fGB".format(bytes / BYTES_PER_GB.toDouble())
    } else {
        "${(bytes / BYTES_PER_MB).toInt()}MB"
    }
