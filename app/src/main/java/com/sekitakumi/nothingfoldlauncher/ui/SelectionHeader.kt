package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingGrays

@Composable
fun SelectionHeader(
    selectedCount: Int,
    confirmLabel: String,
    confirmEnabled: Boolean,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val active = selectedCount > 0
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(if (active) NothingAccent else NothingGrays.OnBase, CircleShape))
            Spacer(Modifier.size(8.dp))
            Text(
                text = "$selectedCount SELECTED",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                color = if (active) Color.White else Color.Gray,
            )
        }
        NothingPrimaryButton(text = confirmLabel, onClick = onConfirm, enabled = confirmEnabled)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SelectionHeaderSelectedPreview() {
    SelectionHeader(selectedCount = 3, confirmLabel = "Done", confirmEnabled = true, onConfirm = {})
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SelectionHeaderEmptyPreview() {
    SelectionHeader(selectedCount = 0, confirmLabel = "Create group", confirmEnabled = false, onConfirm = {})
}
