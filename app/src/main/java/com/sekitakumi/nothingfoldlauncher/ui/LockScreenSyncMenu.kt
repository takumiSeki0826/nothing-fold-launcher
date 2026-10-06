package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun LockScreenSyncMenu(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Black,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { DotMatrixText("Lock screen") },
        text = {
            Row(modifier = Modifier.fillMaxWidth()) {
                DotMatrixText("Sync with lock screen", color = Color.White, modifier = Modifier.weight(1f))
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFD1432B)),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                DotMatrixText("Close", color = Color.Gray)
            }
        },
    )
}
