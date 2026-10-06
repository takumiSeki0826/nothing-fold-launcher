package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sekitakumi.nothingfoldlauncher.data.AppInfo
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingGrays

data class HomeKnobAppAssignment(val tapApp: AppInfo?, val folderApps: List<AppInfo>)

private val ACCENT_COLOR = Color(0xFFD1432B)

@Composable
fun HomeKnobSettingsMenu(
    slots: List<HomeKnobSlot>,
    knobNames: Map<HomeKnobSlot, String>,
    assignments: Map<HomeKnobSlot, HomeKnobAppAssignment>,
    onNameChange: (HomeKnobSlot, String) -> Unit,
    onEditTapApp: (HomeKnobSlot) -> Unit,
    onEditFolder: (HomeKnobSlot) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.Black,
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = { DotMatrixText("Home knob settings") },
        text = {
            Column {
                slots.forEachIndexed { index, slot ->
                    HomeKnobSettingsRow(
                        name = knobNames[slot] ?: slot.defaultLabel,
                        tapAppLabel = assignments[slot]?.tapApp?.label,
                        folderAppCount = assignments[slot]?.folderApps?.size ?: 0,
                        onNameChange = { onNameChange(slot, it) },
                        onEditTapApp = { onEditTapApp(slot) },
                        onEditFolder = { onEditFolder(slot) },
                    )
                    if (index != slots.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = NothingGrays.Base)
                    }
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
private fun HomeKnobSettingsRow(
    name: String,
    tapAppLabel: String?,
    folderAppCount: Int,
    onNameChange: (String) -> Unit,
    onEditTapApp: () -> Unit,
    onEditFolder: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { DotMatrixText("Name", color = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Black,
                unfocusedContainerColor = Color.Black,
            ),
        )
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Row(
                modifier = Modifier.weight(1f).clickable(onClick = onEditTapApp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DotMatrixText(text = "Tap: ", color = ACCENT_COLOR)
                if (tapAppLabel != null) Text(text = tapAppLabel, color = ACCENT_COLOR) else DotMatrixText(text = "+", color = ACCENT_COLOR)
            }
            DotMatrixText(
                text = if (folderAppCount > 0) "Folder: ${appCountLabel(folderAppCount)}" else "Folder: +",
                color = ACCENT_COLOR,
                modifier = Modifier.weight(1f).clickable(onClick = onEditFolder),
            )
        }
    }
}
