package com.sekitakumi.nothingfoldlauncher.ui

import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatusMathTest {

    @Test
    fun `batteryPercent computes percentage from level and scale`() {
        assertEquals(50, batteryPercent(level = 50, scale = 100))
        assertEquals(100, batteryPercent(level = 100, scale = 100))
        assertEquals(0, batteryPercent(level = 0, scale = 100))
    }

    @Test
    fun `batteryPercent returns zero for non-positive scale`() {
        assertEquals(0, batteryPercent(level = 50, scale = 0))
        assertEquals(0, batteryPercent(level = 50, scale = -1))
    }

    @Test
    fun `batteryPercent clamps to 0 to 100`() {
        assertEquals(100, batteryPercent(level = 150, scale = 100))
        assertEquals(0, batteryPercent(level = -10, scale = 100))
    }

    @Test
    fun `signalBars clamps to 0 to 4`() {
        assertEquals(0, signalBars(-1))
        assertEquals(0, signalBars(0))
        assertEquals(4, signalBars(4))
        assertEquals(4, signalBars(10))
    }

    @Test
    fun `networkTypeLabel reports 5G for NR`() {
        assertEquals(
            "5G",
            networkTypeLabel(
                networkType = TelephonyManager.NETWORK_TYPE_NR,
                overrideNetworkType = TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NONE,
            ),
        )
    }

    @Test
    fun `networkTypeLabel reports 5G for non-standalone override even on LTE`() {
        assertEquals(
            "5G",
            networkTypeLabel(
                networkType = TelephonyManager.NETWORK_TYPE_LTE,
                overrideNetworkType = TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA,
            ),
        )
    }

    @Test
    fun `networkTypeLabel reports 5G+ for advanced NR override`() {
        assertEquals(
            "5G+",
            networkTypeLabel(
                networkType = TelephonyManager.NETWORK_TYPE_LTE,
                overrideNetworkType = TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED,
            ),
        )
    }

    @Test
    fun `networkTypeLabel reports 4G for LTE`() {
        assertEquals(
            "4G",
            networkTypeLabel(
                networkType = TelephonyManager.NETWORK_TYPE_LTE,
                overrideNetworkType = TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NONE,
            ),
        )
    }

    @Test
    fun `networkTypeLabel reports 3G for UMTS-family types`() {
        assertEquals(
            "3G",
            networkTypeLabel(
                networkType = TelephonyManager.NETWORK_TYPE_HSPA,
                overrideNetworkType = TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NONE,
            ),
        )
    }

    @Test
    fun `networkTypeLabel returns null for unknown types`() {
        assertNull(
            networkTypeLabel(
                networkType = TelephonyManager.NETWORK_TYPE_UNKNOWN,
                overrideNetworkType = TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NONE,
            ),
        )
    }
}
