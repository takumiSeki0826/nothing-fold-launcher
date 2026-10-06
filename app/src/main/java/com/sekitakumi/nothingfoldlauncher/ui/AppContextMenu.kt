package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun AppContextMenu(
    app: AppInfo,
    isFavorite: Boolean,
    isHidden: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleHidden: () -> Unit,
    onRename: () -> Unit,
    onUninstall: () -> Unit,
    onCreateGroup: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Black,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { DotMatrixText(app.label, ellipsize = true) },
        text = {
            Column {
                TextButton(onClick = onToggleFavorite) {
                    DotMatrixText(
                        if (isFavorite) "Remove from home" else "Show on home",
                        color = Color.White,
                    )
                }
                TextButton(onClick = onToggleHidden) {
                    DotMatrixText(
                        if (isHidden) "Unhide" else "Hide",
                        color = Color.White,
                    )
                }
                TextButton(onClick = onCreateGroup) {
                    DotMatrixText("Create group", color = Color.White)
                }
                TextButton(onClick = onRename) {
                    DotMatrixText("Rename", color = Color.White)
                }
                TextButton(onClick = onUninstall) {
                    DotMatrixText("Uninstall", color = Color.White)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                DotMatrixText("Close", color = Color.Gray)
            }
        },
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
        title = { DotMatrixText("Rename") },
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
                DotMatrixText("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                DotMatrixText("Cancel", color = Color.Gray)
            }
        },
    )
}
