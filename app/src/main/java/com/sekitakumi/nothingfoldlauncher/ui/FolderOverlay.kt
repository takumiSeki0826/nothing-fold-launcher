package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sekitakumi.nothingfoldlauncher.data.AppInfo
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingGrays

private val FOLDER_OVERLAY_ICON_WIDTH = 60.dp
private val FOLDER_OVERLAY_COLUMNS = 4

@Composable
fun FolderOverlay(
    name: String,
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(Color.Black, RoundedCornerShape(12.dp))
                .padding(20.dp),
        ) {
            Text(text = name, color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            if (apps.isEmpty()) {
                Text(text = "No apps yet", color = Color.Gray)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    apps.chunked(FOLDER_OVERLAY_COLUMNS).forEach { rowApps ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowApps.forEach { app ->
                                FolderOverlayAppTile(app = app, onClick = { onAppClick(app) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderOverlayAppTile(app: AppInfo, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(FOLDER_OVERLAY_ICON_WIDTH).combinedClickable(onClick = onClick, onLongClick = {}),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(NothingGrays.Base, RoundedCornerShape(6.dp)),
        ) {
            AppIconDot(modifier = Modifier.align(Alignment.TopEnd))
        }
        Text(
            text = app.label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}
