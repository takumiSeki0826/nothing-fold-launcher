package com.sekitakumi.nothingfoldlauncher.ui

import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun `signalBarWidth sizes 4 bars plus 3 half-width gaps to exactly fill the canvas`() {
        val canvasWidth = 55f
        val barWidth = signalBarWidth(canvasWidth)
        val gap = barWidth * 0.5f
        val totalWidth = 4 * barWidth + 3 * gap
        assertEquals(canvasWidth, totalWidth, 0.001f)
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

    @Test
    fun `isTailscaleCgnatAddress accepts real Tailscale-assigned addresses`() {
        assertTrue(isTailscaleCgnatAddress(byteArrayOf(100, 85, 49, 74)))
    }

    @Test
    fun `isTailscaleCgnatAddress accepts the full 100_64_0_0 to 100_127_255_255 range`() {
        assertTrue(isTailscaleCgnatAddress(byteArrayOf(100, 64, 0, 0)))
        assertTrue(isTailscaleCgnatAddress(byteArrayOf(100, 127, 255.toByte(), 255.toByte())))
    }

    @Test
    fun `isTailscaleCgnatAddress rejects addresses just outside the CGNAT range`() {
        assertFalse(isTailscaleCgnatAddress(byteArrayOf(100, 63, 255.toByte(), 255.toByte())))
        assertFalse(isTailscaleCgnatAddress(byteArrayOf(100, 128.toByte(), 0, 0)))
    }

    @Test
    fun `isTailscaleCgnatAddress rejects other private VPN ranges`() {
        assertFalse(isTailscaleCgnatAddress(byteArrayOf(192.toByte(), 168.toByte(), 1, 1)))
        assertFalse(isTailscaleCgnatAddress(byteArrayOf(10, 0, 0, 1)))
    }

    @Test
    fun `isTailscaleCgnatAddress rejects non-IPv4 addresses`() {
        assertFalse(isTailscaleCgnatAddress(ByteArray(16)))
    }

    @Test
    fun `vpnBadgeFor is NONE when nothing is connected`() {
        assertEquals(
            VpnBadge.NONE,
            vpnBadgeFor(vpnConnected = false, tailscaleConnected = false),
        )
    }

    @Test
    fun `vpnBadgeFor falls back to VPN for a non-Tailscale VPN`() {
        assertEquals(
            VpnBadge.VPN,
            vpnBadgeFor(vpnConnected = true, tailscaleConnected = false),
        )
    }

    @Test
    fun `vpnBadgeFor is TAILSCALE when the tailscale interface is up`() {
        assertEquals(
            VpnBadge.TAILSCALE,
            vpnBadgeFor(vpnConnected = true, tailscaleConnected = true),
        )
    }

    @Test
    fun `isWeakSignal is false just above the cellular threshold`() {
        assertFalse(isWeakSignal(dbm = -109, isWifi = false))
    }

    @Test
    fun `isWeakSignal is true at and below the cellular threshold`() {
        assertTrue(isWeakSignal(dbm = -110, isWifi = false))
        assertTrue(isWeakSignal(dbm = -120, isWifi = false))
    }

    @Test
    fun `isWeakSignal is false just above the wifi threshold`() {
        assertFalse(isWeakSignal(dbm = -79, isWifi = true))
    }

    @Test
    fun `isWeakSignal is true at and below the wifi threshold`() {
        assertTrue(isWeakSignal(dbm = -80, isWifi = true))
        assertTrue(isWeakSignal(dbm = -95, isWifi = true))
    }

    @Test
    fun `preferredSignalDbm uses wifi rssi when connected to wifi`() {
        assertEquals(
            -60,
            preferredSignalDbm(wifiConnected = true, wifiRssi = -60, cellularDbm = -90),
        )
    }

    @Test
    fun `preferredSignalDbm falls back to cellular when wifi rssi is unavailable`() {
        assertEquals(
            -90,
            preferredSignalDbm(wifiConnected = true, wifiRssi = null, cellularDbm = -90),
        )
    }

    @Test
    fun `preferredSignalDbm uses cellular when not connected to wifi`() {
        assertEquals(
            -90,
            preferredSignalDbm(wifiConnected = false, wifiRssi = -60, cellularDbm = -90),
        )
    }

    @Test
    fun `preferredSignalDbm is null when no value is available`() {
        assertNull(preferredSignalDbm(wifiConnected = true, wifiRssi = null, cellularDbm = null))
        assertNull(preferredSignalDbm(wifiConnected = false, wifiRssi = -60, cellularDbm = null))
    }

    @Test
    fun `dailyMobileUsage computes usage from the difference on the same day`() {
        val result = dailyMobileUsage(
            currentTotalBytes = 5_000_000L,
            baselineBytes = 2_000_000L,
            baselineDate = "2026-09-22",
            today = "2026-09-22",
        )
        assertEquals(3_000_000L, result.usageBytes)
        assertEquals(2_000_000L, result.newBaselineBytes)
        assertEquals("2026-09-22", result.newBaselineDate)
    }

    @Test
    fun `dailyMobileUsage resets the baseline when the date has changed`() {
        val result = dailyMobileUsage(
            currentTotalBytes = 9_000_000L,
            baselineBytes = 2_000_000L,
            baselineDate = "2026-09-21",
            today = "2026-09-22",
        )
        assertEquals(0L, result.usageBytes)
        assertEquals(9_000_000L, result.newBaselineBytes)
        assertEquals("2026-09-22", result.newBaselineDate)
    }

    @Test
    fun `dailyMobileUsage resets the baseline when there is no prior baseline`() {
        val result = dailyMobileUsage(
            currentTotalBytes = 4_000_000L,
            baselineBytes = null,
            baselineDate = null,
            today = "2026-09-22",
        )
        assertEquals(0L, result.usageBytes)
        assertEquals(4_000_000L, result.newBaselineBytes)
        assertEquals("2026-09-22", result.newBaselineDate)
    }

    @Test
    fun `dailyMobileUsage treats a reboot's lower counter as usage since reboot`() {
        val result = dailyMobileUsage(
            currentTotalBytes = 500_000L,
            baselineBytes = 2_000_000L,
            baselineDate = "2026-09-22",
            today = "2026-09-22",
        )
        assertEquals(500_000L, result.usageBytes)
        assertEquals(0L, result.newBaselineBytes)
        assertEquals("2026-09-22", result.newBaselineDate)
    }

    @Test
    fun `formatDataUsage shows megabytes just below the gigabyte threshold`() {
        assertEquals("999MB", formatDataUsage(999_000_000L))
    }

    @Test
    fun `formatDataUsage shows gigabytes at and above the threshold`() {
        assertEquals("1.0GB", formatDataUsage(1_000_000_000L))
        assertEquals("1.2GB", formatDataUsage(1_200_000_000L))
    }

    @Test
    fun `formatDataUsage shows zero megabytes for no usage`() {
        assertEquals("0MB", formatDataUsage(0L))
    }
}
