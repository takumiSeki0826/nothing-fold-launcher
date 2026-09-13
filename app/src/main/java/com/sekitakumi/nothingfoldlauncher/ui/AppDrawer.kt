package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sekitakumi.nothingfoldlauncher.data.AppInfo

@Composable
fun AppDrawer(
    apps: List<AppInfo>,
    query: String,
    favorites: Set<String>,
    onQueryChange: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search", color = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Black,
                unfocusedContainerColor = Color.Black,
            ),
        )

        Text(
            text = "長押しでホーム画面への表示/非表示を切り替え",
            color = Color.Gray,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 8.dp),
        )

        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
            itemsIndexed(apps, key = { _, app -> app.packageName }) { index, app ->
                AppRow(
                    app = app,
                    index = index,
                    isFavorite = app.packageName in favorites,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) },
                )
            }
        }
    }
}

@Composable
private fun AppRow(
    app: AppInfo,
    index: Int,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(40.dp)
                    .background(colorForAppIndex(index), RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFF333333), RoundedCornerShape(10.dp)),
            )
            Text(
                text = app.label,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        if (isFavorite) {
            Text(text = "★", color = Color(0xFFD1432B))
        }
    }
}
