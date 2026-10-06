package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingGrays

internal val NothingAccent = Color(0xFFD1432B)

@Composable
fun NothingDialogPanel(
    title: String,
    onDismiss: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val shape = RoundedCornerShape(24.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 420.dp)
                .background(NothingGrays.Base, shape)
                .border(1.dp, NothingGrays.OnBase, shape)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(NothingAccent, CircleShape))
                Spacer(Modifier.size(8.dp))
                DotMatrixText(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp,
                )
            }
            content()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                content = actions,
            )
        }
    }
}

@Composable
fun NothingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        label = { DotMatrixText(label, color = Color.Gray) },
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.Black,
            unfocusedContainerColor = Color.Black,
            focusedIndicatorColor = NothingAccent,
            unfocusedIndicatorColor = NothingGrays.OnBase,
            cursorColor = NothingAccent,
        ),
    )
}

@Composable
fun NothingActionRow(text: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.Black)
            .border(1.dp, NothingGrays.OnBase, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DotMatrixText(
            text = text,
            color = Color.White,
            modifier = Modifier.weight(1f),
        )
        DotMatrixIcon(rows = DotIcons.CHEVRON_RIGHT, color = Color.Gray, dotSize = 2.dp)
    }
}

@Composable
fun NothingOutlinedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, NothingAccent, shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        DotMatrixText(text = text, color = NothingAccent)
    }
}

@Composable
fun NothingPrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (enabled) NothingAccent else NothingGrays.OnBase)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        DotMatrixText(text = text, color = Color.White)
    }
}

@Composable
fun NothingGhostButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        DotMatrixText(text = text, color = Color.Gray)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun NothingDialogPanelPreview() {
    NothingDialogPanel(
        title = "Sample dialog",
        onDismiss = {},
        actions = {
            NothingGhostButton("Cancel", {})
            NothingPrimaryButton("Create", {})
        },
    ) {
        NothingTextField(value = "Name", onValueChange = {}, label = "Name")
        NothingActionRow(text = "Edit apps (3)", onClick = {})
        NothingOutlinedButton(text = "Delete", onClick = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun NothingPrimaryButtonPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(16.dp)) {
        NothingPrimaryButton("Enabled", {}, enabled = true)
        NothingPrimaryButton("Disabled", {}, enabled = false)
    }
}
