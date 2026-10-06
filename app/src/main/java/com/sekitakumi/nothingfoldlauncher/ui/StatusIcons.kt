package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatusIcons(
    batteryPercent: Int,
    isCharging: Boolean,
    wifiConnected: Boolean,
    signalBars: Int?,
    networkType: String? = null,
    vpnConnected: Boolean = false,
    tailscaleConnected: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val vpnBadge = vpnBadgeFor(vpnConnected = vpnConnected, tailscaleConnected = tailscaleConnected)
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (signalBars != null) {
            DotMatrixIcon(rows = signalBarsPattern(signalBars), color = Color.White, dotSize = 1.6.dp)
            Spacer(modifier = Modifier.width(10.dp))
        }
        if (!wifiConnected && networkType != null) {
            DotMatrixText(text = networkType, color = Color.White, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(10.dp))
        }
        if (vpnBadge == VpnBadge.TAILSCALE) {
            DotMatrixIcon(rows = DotIcons.TAILSCALE, color = Color.White, dotSize = 3.dp)
            Spacer(modifier = Modifier.width(10.dp))
        } else if (vpnBadge == VpnBadge.VPN) {
            DotMatrixText(
                text = "VPN",
                color = Color.White,
                fontSize = 8.sp,
                modifier = Modifier
                    .border(width = 1.dp, color = Color.White, shape = RoundedCornerShape(3.dp))
                    .padding(horizontal = 3.dp, vertical = 1.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        if (wifiConnected) {
            DotMatrixIcon(rows = DotIcons.WIFI, color = Color.White, dotSize = 1.4.dp)
            Spacer(modifier = Modifier.width(10.dp))
        }
        if (isCharging) {
            DotMatrixIcon(rows = DotIcons.BOLT, color = Color.White, dotSize = 1.4.dp)
            Spacer(modifier = Modifier.width(3.dp))
        }
        DotMatrixIcon(
            rows = batteryPattern(batteryPercent),
            color = if (!isCharging && batteryPercent <= LOW_BATTERY_PERCENT) LOW_BATTERY_COLOR else Color.White,
            dotSize = 1.4.dp,
        )
        Spacer(modifier = Modifier.width(4.dp))
        DotMatrixText(text = "$batteryPercent%", fontSize = 12.sp, color = Color.White)
    }
}

private const val LOW_BATTERY_PERCENT = 15
private val LOW_BATTERY_COLOR = Color(0xFFD1432B)
