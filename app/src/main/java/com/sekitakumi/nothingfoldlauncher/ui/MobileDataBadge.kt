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

/**
 * Small pill showing today's mobile data usage so far (RX+TX combined, reset at
 * midnight), placed below [SignalBadge] on the home screen's expanded layout.
 */
@Composable
fun MobileDataBadge(usageBytes: Long, modifier: Modifier = Modifier) {
    Text(
        text = formatDataUsage(usageBytes),
        color = Color.White,
        fontSize = 12.sp,
        modifier = modifier
            .background(NothingGrays.Base, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}
