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
}
