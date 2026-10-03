package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.sekitakumi.nothingfoldlauncher.data.AppFolder
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingGrays

private val ACCENT_COLOR = Color(0xFFD1432B)

@Composable
fun HomeAppGridSettingsMenu(
    folders: List<AppFolder>,
    onAddFolder: () -> Unit,
    onEditFolder: (AppFolder) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Black,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { Text("App list settings") },
        text = {
            Column {
                folders.forEach { folder ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = folder.name, color = Color.White)
                        Text(
                            text = "Edit",
                            color = ACCENT_COLOR,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable(onClick = { onEditFolder(folder) }),
                        )
                    }
                    HorizontalDivider(color = NothingGrays.Base)
                }
                Text(
                    text = "+ Add folder",
                    color = ACCENT_COLOR,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable(onClick = onAddFolder),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.Gray)
            }
        },
    )
}
