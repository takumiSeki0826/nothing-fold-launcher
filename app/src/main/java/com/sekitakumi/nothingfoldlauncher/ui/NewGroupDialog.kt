package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun NewGroupDialog(
    appCount: Int,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("Group") }

    NothingDialogPanel(
        title = "New group (${appCountLabel(appCount)})",
        onDismiss = onDismiss,
        actions = {
            NothingGhostButton("Cancel", onDismiss)
            NothingPrimaryButton("Create", { onConfirm(name.trim()) }, enabled = name.isNotBlank())
        },
    ) {
        NothingTextField(value = name, onValueChange = { name = it }, label = "Name")
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun NewGroupDialogPreview() {
    NewGroupDialog(appCount = 3, onConfirm = {}, onDismiss = {})
}
