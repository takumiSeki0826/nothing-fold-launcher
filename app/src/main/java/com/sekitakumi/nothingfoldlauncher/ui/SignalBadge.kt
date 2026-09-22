package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingGrays

private val SIGNAL_BADGE_WEAK_COLOR = Color(0xFFD1432B)

/**
 * Small pill showing the raw signal strength in dBm, placed at the top-right of the
 * home screen's expanded layout (mirroring the battery/status row on the opposite
 * side). Renders nothing when [dbm] is unavailable (missing permission, no signal, etc.).
 */
@Composable
fun SignalBadge(dbm: Int?, isWifi: Boolean, modifier: Modifier = Modifier) {
    if (dbm == null) return

    val textColor = if (isWeakSignal(dbm, isWifi)) SIGNAL_BADGE_WEAK_COLOR else Color.White

    Text(
        text = "${dbm}dBm",
        color = textColor,
        fontSize = 12.sp,
        modifier = modifier
            .background(NothingGrays.Base, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}
