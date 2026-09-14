package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
    brightnessRatio: Float,
    onBrightnessRatioChange: (Float) -> Unit,
    batteryPercent: Int,
    isCharging: Boolean,
    wifiConnected: Boolean,
    signalBars: Int?,
    networkType: String?,
    nowPlaying: NowPlayingState?,
    nowPlayingPermissionGranted: Boolean,
    onTogglePlayPause: () -> Unit,
    onRequestNowPlayingPermission: () -> Unit,
    onNowPlayingClick: () -> Unit,
    onNowPlayingLongClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onCalendarLongClick: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    isExpandedWidth: Boolean,
    modifier: Modifier = Modifier,
) {
    val gridColumns = homeGridColumns(isExpandedWidth)

    if (isExpandedWidth) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(
                modifier = Modifier.weight(4f).fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
            ) {
                ClockRow(
                    batteryPercent = batteryPercent,
                    isCharging = isCharging,
                    wifiConnected = wifiConnected,
                    signalBars = signalBars,
                    networkType = networkType,
                )

                Spacer(modifier = Modifier.height(20.dp))

                CalendarWidget(
                    onClick = onCalendarClick,
                    onLongClick = onCalendarLongClick,
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                NowPlayingWidget(
                    nowPlaying = nowPlaying,
                    permissionGranted = nowPlayingPermissionGranted,
                    onTogglePlayPause = onTogglePlayPause,
                    onRequestPermission = onRequestNowPlayingPermission,
                    onClick = onNowPlayingClick,
                    onLongClick = onNowPlayingLongClick,
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                )

                if (errorMessage != null) {
                    Text(text = errorMessage, color = Color.White, textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.height(28.dp))

                SlidersRow(
                    volumeRatio = volumeRatio,
                    onVolumeRatioChange = onVolumeRatioChange,
                    brightnessRatio = brightnessRatio,
                    onBrightnessRatioChange = onBrightnessRatioChange,
                )
            }

            AppGrid(
                apps = apps,
                columns = gridColumns,
                onAppClick = onAppClick,
                onAppLongClick = onAppLongClick,
                modifier = Modifier.weight(6f).fillMaxHeight(),
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            ClockRow(
                batteryPercent = batteryPercent,
                isCharging = isCharging,
                wifiConnected = wifiConnected,
                signalBars = signalBars,
                networkType = networkType,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CalendarWidget(
                    onClick = onCalendarClick,
                    onLongClick = onCalendarLongClick,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                NowPlayingWidget(
                    nowPlaying = nowPlaying,
                    permissionGranted = nowPlayingPermissionGranted,
                    onTogglePlayPause = onTogglePlayPause,
                    onRequestPermission = onRequestNowPlayingPermission,
                    onClick = onNowPlayingClick,
                    onLongClick = onNowPlayingLongClick,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }

            if (errorMessage != null) {
                Text(text = errorMessage, color = Color.White, textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(3f), contentAlignment = Alignment.Center) {
                    SlidersRow(
                        volumeRatio = volumeRatio,
                        onVolumeRatioChange = onVolumeRatioChange,
                        brightnessRatio = brightnessRatio,
                        onBrightnessRatioChange = onBrightnessRatioChange,
                    )
                }

                AppGrid(
                    apps = apps,
                    columns = gridColumns,
                    onAppClick = onAppClick,
                    onAppLongClick = onAppLongClick,
                    modifier = Modifier.weight(7f),
                )
            }
        }
    }
}

@Composable
private fun ClockRow(
    batteryPercent: Int,
    isCharging: Boolean,
    wifiConnected: Boolean,
    signalBars: Int?,
    networkType: String?,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Clock(modifier = Modifier.weight(1f))
        StatusIcons(
            batteryPercent = batteryPercent,
            isCharging = isCharging,
            wifiConnected = wifiConnected,
            signalBars = signalBars,
            networkType = networkType,
        )
    }
}

@Composable
private fun SlidersRow(
    volumeRatio: Float,
    onVolumeRatioChange: (Float) -> Unit,
    brightnessRatio: Float,
    onBrightnessRatioChange: (Float) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        VolumeSlider(ratio = volumeRatio, onRatioChange = onVolumeRatioChange)
        FaderSlider(
            label = "Bright",
            ratio = brightnessRatio,
            onRatioChange = onBrightnessRatioChange,
        )
    }
}

@Composable
private fun AppGrid(
    apps: List<AppInfo>,
    columns: Int,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        apps.chunked(columns).forEachIndexed { rowIndex, rowApps ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (columnIndex in 0 until columns) {
                    val app = rowApps.getOrNull(columnIndex)
                    if (app != null) {
                        val index = rowIndex * columns + columnIndex
                        AppIconTile(
                            app = app,
                            index = index,
                            onClick = { onAppClick(app) },
                            onLongClick = { onAppLongClick(app) },
                            modifier = Modifier.width(60.dp),
                        )
                    } else {
                        Spacer(modifier = Modifier.width(60.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AppIconTile(
    app: AppInfo,
    index: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(colorForAppIndex(index), RoundedCornerShape(18.dp))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(18.dp)),
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
