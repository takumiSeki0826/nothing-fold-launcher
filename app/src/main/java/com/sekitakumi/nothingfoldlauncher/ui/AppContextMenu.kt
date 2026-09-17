package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sekitakumi.nothingfoldlauncher.data.AppInfo

private val ACCENT_COLOR = Color(0xFFD1432B)

@Composable
fun AppContextMenu(
    app: AppInfo,
    isFavorite: Boolean,
    isHidden: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleHidden: () -> Unit,
    onRename: () -> Unit,
    onChangeColor: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Black,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { Text(app.label) },
        text = {
            Column {
                TextButton(onClick = onToggleFavorite) {
                    Text(
                        if (isFavorite) "Remove from home" else "Show on home",
                        color = Color.White,
                    )
                }
                TextButton(onClick = onToggleHidden) {
                    Text(
                        if (isHidden) "Unhide" else "Hide",
                        color = Color.White,
                    )
                }
                TextButton(onClick = onRename) {
                    Text("Rename", color = Color.White)
                }
                TextButton(onClick = onChangeColor) {
                    Text("Change color", color = Color.White)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.Gray)
            }
        },
    )
}

@Composable
fun IconColorPickerDialog(
    app: AppInfo,
    selectedColor: IconPaletteColor?,
    onSelectColor: (IconPaletteColor) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Black,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { Text("Change color") },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (color in IconPaletteColor.entries) {
                    ColorSwatch(
                        color = color,
                        selected = color == selectedColor,
                        onClick = { onSelectColor(color) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.Gray)
            }
        },
    )
}

@Composable
private fun ColorSwatch(
    color: IconPaletteColor,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(onClick = onClick)
            .background(colorValue(color), CircleShape)
            .border(if (selected) 2.dp else 0.dp, ACCENT_COLOR, CircleShape),
    )
}

@Composable
fun RenameAppDialog(
    app: AppInfo,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember(app.packageName) { mutableStateOf(app.label) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Black,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { Text("Rename") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Black,
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text) }) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        },
    )
}
