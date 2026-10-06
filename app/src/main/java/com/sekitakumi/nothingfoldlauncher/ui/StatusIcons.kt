package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sekitakumi.nothingfoldlauncher.R
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingGrays

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
            SignalBarsIcon(bars = signalBars)
            Spacer(modifier = Modifier.width(10.dp))
        }
        if (!wifiConnected && networkType != null) {
            Text(text = networkType, color = Color.White, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(10.dp))
        }
        if (vpnBadge == VpnBadge.TAILSCALE) {
            Icon(
                painter = painterResource(id = R.drawable.ic_tailscale),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.width(12.dp).height(12.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
        } else if (vpnBadge == VpnBadge.VPN) {
            Text(
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
            Icon(
                imageVector = Icons.Filled.Wifi,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.width(18.dp).height(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        BatteryIcon(percent = batteryPercent, isCharging = isCharging)
        Spacer(modifier = Modifier.width(4.dp))
        DotMatrixText(text = "$batteryPercent%", fontSize = 12.sp, color = Color.White)
    }
}

@Composable
private fun SignalBarsIcon(bars: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.width(18.dp).height(14.dp)) {
        val barWidth = signalBarWidth(size.width)
        val gap = barWidth * 0.5f
        for (i in 0 until 4) {
            val barHeight = size.height * (i + 1) / 4f
            val color = if (i < bars) Color.White else NothingGrays.Base
            drawRoundRect(
                color = color,
                topLeft = Offset(x = i * (barWidth + gap), y = size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth * 0.3f, barWidth * 0.3f),
            )
        }
    }
}

/** iOS-style battery icon (rounded outline + right nub + fill + lightning bolt while charging) */
@Composable
private fun BatteryIcon(percent: Int, isCharging: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.width(26.dp).height(13.dp)) {
        val nubWidth = size.width * 0.08f
        val bodyWidth = size.width - nubWidth - 2.dp.toPx()
        val strokeWidth = 1.2.dp.toPx()
        val cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(0f, 0f),
            size = Size(bodyWidth, size.height),
            cornerRadius = cornerRadius,
            style = Stroke(width = strokeWidth),
        )

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(bodyWidth + 2.dp.toPx(), size.height * 0.3f),
            size = Size(nubWidth, size.height * 0.4f),
            cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx()),
        )

        val inset = strokeWidth * 1.5f
        val fillWidth = ((bodyWidth - inset * 2) * (percent / 100f)).coerceIn(0f, bodyWidth - inset * 2)
        val fillColor = when {
            isCharging -> Color.White
            percent <= 15 -> Color(0xFFD1432B)
            else -> Color.White
        }
        drawRoundRect(
            color = fillColor,
            topLeft = Offset(inset, inset),
            size = Size(fillWidth, size.height - inset * 2),
            cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx()),
        )

        if (isCharging) {
            drawLightningBolt(bodyWidth = bodyWidth)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLightningBolt(bodyWidth: Float) {
    val h = size.height
    val cx = bodyWidth / 2f
    val boltWidth = h * 0.55f
    val path = Path().apply {
        moveTo(cx + boltWidth * 0.12f, 0f)
        lineTo(cx - boltWidth * 0.35f, h * 0.58f)
        lineTo(cx - boltWidth * 0.05f, h * 0.58f)
        lineTo(cx - boltWidth * 0.12f, h)
        lineTo(cx + boltWidth * 0.35f, h * 0.42f)
        lineTo(cx + boltWidth * 0.05f, h * 0.42f)
        close()
    }
    drawPath(path, color = Color.Black)
}
