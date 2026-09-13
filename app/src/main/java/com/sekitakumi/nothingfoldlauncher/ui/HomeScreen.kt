package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sekitakumi.nothingfoldlauncher.data.AppInfo

@Composable
fun HomeScreen(
    apps: List<AppInfo>,
    errorMessage: String?,
    volumeRatio: Float,
    onVolumeRatioChange: (Float) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
    ) {
        VolumeBar(ratio = volumeRatio, onRatioChange = onVolumeRatioChange)

        Box(modifier = Modifier.fillMaxWidth().weight(0.2f), contentAlignment = Alignment.CenterStart) {
            Clock()
        }

        if (errorMessage != null) {
            Text(text = errorMessage, color = Color.White, textAlign = TextAlign.Center)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth().weight(0.8f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(apps, key = { _, app -> app.packageName }) { index, app ->
                AppIconTile(app = app, index = index, onClick = { onAppClick(app) })
            }
        }
    }
}

@Composable
private fun AppIconTile(app: AppInfo, index: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(colorForAppIndex(index), RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(20.dp)),
        )
        Text(
            text = app.label,
            color = Color.White,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}
