package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sekitakumi.nothingfoldlauncher.data.AppFolder
import com.sekitakumi.nothingfoldlauncher.data.AppInfo
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingGrays
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

private val HOME_KNOB_DIAMETER = 96.dp
private val APP_ICON_WIDTH = 60.dp

@Composable
fun HomeScreen(
    items: List<HomeGridItem>,
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
    onFolderClick: (AppFolder) -> Unit,
    onFolderLongClick: (AppFolder) -> Unit,
    onAppGridSettingsLongPress: () -> Unit,
    isExpandedWidth: Boolean,
    homeKnobNames: Map<HomeKnobSlot, String>,
    onHomeKnobTap: (HomeKnobSlot) -> Unit,
    onHomeKnobLongPress: (HomeKnobSlot) -> Unit,
    onHomeKnobSettingsLongPress: () -> Unit,
    iconColorFor: (AppInfo) -> IconPaletteColor?,
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
                modifier = Modifier.weight(4f).fillMaxHeight().offset(x = 48.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            ) {
                ClockRow(
                    batteryPercent = batteryPercent,
                    isCharging = isCharging,
                    wifiConnected = wifiConnected,
                    signalBars = signalBars,
                    networkType = networkType,
                )

                CalendarWidget(
                    onClick = onCalendarClick,
                    onLongClick = onCalendarLongClick,
                    modifier = Modifier.fillMaxWidth(),
                )

                NowPlayingWidget(
                    nowPlaying = nowPlaying,
                    permissionGranted = nowPlayingPermissionGranted,
                    onTogglePlayPause = onTogglePlayPause,
                    onRequestPermission = onRequestNowPlayingPermission,
                    onClick = onNowPlayingClick,
                    onLongClick = onNowPlayingLongClick,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (errorMessage != null) {
                    Text(text = errorMessage, color = Color.White, textAlign = TextAlign.Center)
                }
            }

            BoxWithConstraints(modifier = Modifier.weight(6f).fillMaxHeight().padding(start = 74.dp)) {
                // Match the horizontal inset the app grid gets from its own
                // Arrangement.SpaceEvenly (gridColumns items -> gridColumns + 1 equal gaps),
                // so the sliders/knobs row lines up with the app icons below it.
                val gridEdgeInset = (maxWidth - APP_ICON_WIDTH * gridColumns) / (gridColumns + 1)

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.weight(1.1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = gridEdgeInset)
                            .pointerInput(Unit) {
                                detectTapGestures(onLongPress = { onHomeKnobSettingsLongPress() })
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SlidersRow(
                            volumeRatio = volumeRatio,
                            onVolumeRatioChange = onVolumeRatioChange,
                            brightnessRatio = brightnessRatio,
                            onBrightnessRatioChange = onBrightnessRatioChange,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (slot in listOf(HomeKnobSlot.HOME_1, HomeKnobSlot.HOME_2)) {
                                RotaryKnob(
                                    angleDeg = 0f,
                                    label = homeKnobNames[slot] ?: slot.defaultLabel,
                                    onTap = { onHomeKnobTap(slot) },
                                    onLongPress = { onHomeKnobLongPress(slot) },
                                    diameter = HOME_KNOB_DIAMETER,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTapGestures(onLongPress = { onAppGridSettingsLongPress() })
                            },
                    ) {
                        AppGrid(
                            items = items.take(expandedGridMaxApps()),
                            columns = gridColumns,
                            iconColorFor = iconColorFor,
                            onAppClick = onAppClick,
                            onAppLongClick = onAppLongClick,
                            onFolderClick = onFolderClick,
                            onFolderLongClick = onFolderLongClick,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.weight(0.9f))
                }
            }
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

                Box(
                    modifier = Modifier
                        .weight(7f)
                        .pointerInput(Unit) {
                            detectTapGestures(onLongPress = { onAppGridSettingsLongPress() })
                        },
                ) {
                    AppGrid(
                        items = items,
                        columns = gridColumns,
                        iconColorFor = iconColorFor,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick,
                        onFolderClick = onFolderClick,
                        onFolderLongClick = onFolderLongClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
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
    items: List<HomeGridItem>,
    columns: Int,
    iconColorFor: (AppInfo) -> IconPaletteColor?,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onFolderClick: (AppFolder) -> Unit,
    onFolderLongClick: (AppFolder) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items.chunked(columns).forEachIndexed { rowIndex, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (columnIndex in 0 until columns) {
                    val item = rowItems.getOrNull(columnIndex)
                    when (item) {
                        is HomeGridItem.AppItem -> AppIconTile(
                            app = item.app,
                            onClick = { onAppClick(item.app) },
                            onLongClick = { onAppLongClick(item.app) },
                            modifier = Modifier.width(APP_ICON_WIDTH),
                        )
                        is HomeGridItem.FolderItem -> FolderKnobTile(
                            folder = item.folder,
                            onClick = { onFolderClick(item.folder) },
                            onLongClick = { onFolderLongClick(item.folder) },
                            modifier = Modifier.width(APP_ICON_WIDTH),
                        )
                        null -> Spacer(modifier = Modifier.width(APP_ICON_WIDTH))
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderKnobTile(
    folder: AppFolder,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RotaryKnob(
        angleDeg = 0f,
        label = folder.name,
        onTap = onClick,
        onLongPress = onLongClick,
        diameter = APP_ICON_WIDTH,
        modifier = modifier,
    )
}

private val APP_ICON_CORNER_SHAPE = RoundedCornerShape(6.dp)
private val APP_ICON_BACKGROUND_COLOR = NothingGrays.Base
private val APP_ICON_DOT_COLOR = Color(0xFFD1432B)
private val APP_ICON_DOT_SIZE = 8.dp
private val APP_ICON_DOT_INSET = 4.dp
private const val APP_ICON_DOT_DROP_DISTANCE_MAX_DP = 44f
private const val APP_ICON_DOT_DROP_DISTANCE_MIN_FRACTION = 0.7f

// The two bounces stay well below the top (as a fraction of the drop distance, where
// 1f is the floor and 0f is the top) so the motion clearly reads as landing near the
// bottom, before the final segment does the one big climb back up to the top.
private const val APP_ICON_DOT_FIRST_BOUNCE_DEPTH_FRACTION = 0.55f
private const val APP_ICON_DOT_SECOND_IMPACT_DEPTH_FRACTION = 0.78f

private const val APP_ICON_DOT_FALL_DURATION_MAX_MS = 260
private const val APP_ICON_DOT_SMALL_BOUNCE_DURATION_MAX_MS = 180
private const val APP_ICON_DOT_FINAL_RISE_DURATION_MAX_MS = 320

// Falls accelerate like gravity; rises decelerate like leaving the floor against gravity.
private val APP_ICON_DOT_FALL_EASING = CubicBezierEasing(0.55f, 0f, 1f, 0.45f)
private val APP_ICON_DOT_RISE_EASING = CubicBezierEasing(0f, 0.55f, 0.45f, 1f)

private fun scaledDurationMs(distanceDp: Float, maxDurationMs: Int): Int =
    (maxDurationMs * sqrt((distanceDp / APP_ICON_DOT_DROP_DISTANCE_MAX_DP).coerceIn(0.05f, 1f)))
        .roundToInt()
        .coerceAtLeast(60)

@Composable
private fun AppIconTile(
    app: AppInfo,
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
                .background(APP_ICON_BACKGROUND_COLOR, APP_ICON_CORNER_SHAPE),
        ) {
            val dotWobbleOffset = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                val dropDistance = APP_ICON_DOT_DROP_DISTANCE_MAX_DP * (
                    APP_ICON_DOT_DROP_DISTANCE_MIN_FRACTION +
                        Random.nextFloat() * (1f - APP_ICON_DOT_DROP_DISTANCE_MIN_FRACTION)
                    )
                val firstBounceDepth = dropDistance * APP_ICON_DOT_FIRST_BOUNCE_DEPTH_FRACTION
                val secondImpactDepth = dropDistance * APP_ICON_DOT_SECOND_IMPACT_DEPTH_FRACTION

                // Fall from the top like a dropped ball, accelerating into the floor.
                dotWobbleOffset.animateTo(
                    targetValue = dropDistance,
                    animationSpec = tween(
                        durationMillis = scaledDurationMs(dropDistance, APP_ICON_DOT_FALL_DURATION_MAX_MS),
                        easing = APP_ICON_DOT_FALL_EASING,
                    ),
                )
                // Small bounce off the floor, then a smaller second impact - both still
                // near the bottom.
                dotWobbleOffset.animateTo(
                    targetValue = firstBounceDepth,
                    animationSpec = tween(
                        durationMillis = scaledDurationMs(dropDistance - firstBounceDepth, APP_ICON_DOT_SMALL_BOUNCE_DURATION_MAX_MS),
                        easing = APP_ICON_DOT_RISE_EASING,
                    ),
                )
                dotWobbleOffset.animateTo(
                    targetValue = secondImpactDepth,
                    animationSpec = tween(
                        durationMillis = scaledDurationMs(secondImpactDepth - firstBounceDepth, APP_ICON_DOT_SMALL_BOUNCE_DURATION_MAX_MS),
                        easing = APP_ICON_DOT_FALL_EASING,
                    ),
                )
                // Then climb all the way back up to rest at the top.
                dotWobbleOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = scaledDurationMs(secondImpactDepth, APP_ICON_DOT_FINAL_RISE_DURATION_MAX_MS),
                        easing = APP_ICON_DOT_RISE_EASING,
                    ),
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = APP_ICON_DOT_INSET, end = APP_ICON_DOT_INSET)
                    .offset(y = dotWobbleOffset.value.dp)
                    .size(APP_ICON_DOT_SIZE)
                    .background(APP_ICON_DOT_COLOR, CircleShape),
            )
        }
        Text(
            text = app.label,
            color = Color.White,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}
