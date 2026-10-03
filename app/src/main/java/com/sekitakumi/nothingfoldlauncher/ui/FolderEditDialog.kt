package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun FolderEditDialog(
    name: String,
    appCount: Int,
    onNameChange: (String) -> Unit,
    onEditApps: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    NothingDialogPanel(
        title = "Folder settings",
        onDismiss = onDismiss,
        actions = {
            NothingGhostButton("Close", onDismiss)
        },
    ) {
        NothingTextField(value = name, onValueChange = onNameChange, label = "Name", singleLine = true)
        NothingActionRow(text = "Edit apps ($appCount)", onClick = onEditApps)
        NothingOutlinedButton(text = "Delete folder", onClick = onDelete)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun FolderEditDialogPreview() {
    FolderEditDialog(
        name = "Downloads",
        appCount = 3,
        onNameChange = {},
        onEditApps = {},
        onDelete = {},
        onDismiss = {},
    )
}
