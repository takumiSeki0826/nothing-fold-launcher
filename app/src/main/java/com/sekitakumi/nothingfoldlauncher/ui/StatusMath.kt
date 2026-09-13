package com.sekitakumi.nothingfoldlauncher.ui

import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager

fun batteryPercent(level: Int, scale: Int): Int {
    if (scale <= 0) return 0
    return ((level * 100) / scale).coerceIn(0, 100)
}

fun signalBars(level: Int): Int = level.coerceIn(0, 4)

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
