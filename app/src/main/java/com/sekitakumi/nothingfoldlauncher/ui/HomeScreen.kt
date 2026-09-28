package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.sekitakumi.nothingfoldlauncher.data.AppFolder
import com.sekitakumi.nothingfoldlauncher.data.AppInfo
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingGrays

private val HOME_KNOB_DIAMETER = 96.dp
private val APP_ICON_WIDTH = 60.dp

// Tuned to line up with the battery/status row's text baseline in the left column's ClockRow.
private val SIGNAL_BADGE_TOP_OFFSET = 115.dp

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
    signalDbm: Int?,
    dailyMobileDataUsage: Long,
    networkType: String?,
    vpnConnected: Boolean,
    tailscaleConnected: Boolean,
    weather: WeatherState?,
    onWeatherClick: () -> Unit,
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
    onReorder: (from: HomeGridItem, to: HomeGridItem?) -> Unit,
    isExpandedWidth: Boolean,
    homeKnobNames: Map<HomeKnobSlot, String>,
    onHomeKnobTap: (HomeKnobSlot) -> Unit,
    onHomeKnobLongPress: (HomeKnobSlot) -> Unit,
    onHomeKnobSettingsLongPress: () -> Unit,
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
                    vpnConnected = vpnConnected,
                    tailscaleConnected = tailscaleConnected,
                    weather = weather,
                    onWeatherClick = onWeatherClick,
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
                            slotCount = null,
                            onAppClick = onAppClick,
                            onAppLongClick = onAppLongClick,
                            onFolderClick = onFolderClick,
                            onFolderLongClick = onFolderLongClick,
                            onReorder = onReorder,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.weight(0.9f))
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = SIGNAL_BADGE_TOP_OFFSET, end = gridEdgeInset),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    SignalBadge(dbm = signalDbm, isWifi = wifiConnected)
                    MobileDataBadge(usageBytes = dailyMobileDataUsage)
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
                vpnConnected = vpnConnected,
                tailscaleConnected = tailscaleConnected,
                weather = weather,
                onWeatherClick = onWeatherClick,
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
                        items = items.take(coverGridMaxItems()),
                        columns = gridColumns,
                        slotCount = coverGridMaxItems(),
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick,
                        onFolderClick = onFolderClick,
                        onFolderLongClick = onFolderLongClick,
                        onReorder = onReorder,
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
    vpnConnected: Boolean,
    tailscaleConnected: Boolean,
    weather: WeatherState?,
    onWeatherClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.Top) {
        Clock(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            StatusIcons(
                batteryPercent = batteryPercent,
                isCharging = isCharging,
                wifiConnected = wifiConnected,
                signalBars = signalBars,
                networkType = networkType,
                vpnConnected = vpnConnected,
                tailscaleConnected = tailscaleConnected,
            )
            WeatherWidget(weather = weather, onClick = onWeatherClick)
        }
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
    slotCount: Int?,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onFolderClick: (AppFolder) -> Unit,
    onFolderLongClick: (AppFolder) -> Unit,
    onReorder: (from: HomeGridItem, to: HomeGridItem?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalSlots = slotCount ?: items.size
    val slotBounds = remember { mutableStateMapOf<Int, Rect>() }
    val slotCoordinates = remember { mutableStateMapOf<Int, LayoutCoordinates>() }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragPositionWindow by remember { mutableStateOf(Offset.Zero) }
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    var pressedIndex by remember { mutableStateOf<Int?>(null) }
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        (0 until totalSlots).chunked(columns).forEach { rowIndices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (index in rowIndices) {
                    val item = items.getOrNull(index)
                    val isDraggingThis = draggingIndex == index
                    val isHovered = hoveredIndex == index && !isDraggingThis

                    val pickUp: () -> Unit = {
                        draggingIndex = index
                        dragPositionWindow = slotBounds[index]?.center ?: Offset.Zero
                        hoveredIndex = index
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    val moveDrag: (Offset) -> Unit = { windowPosition ->
                        dragPositionWindow = windowPosition
                        hoveredIndex = nearestSlotIndex(windowPosition, (0 until totalSlots).map { slotBounds[it] })
                    }
                    val endDrag: (Boolean) -> Unit = { commit ->
                        val target = hoveredIndex
                        draggingIndex = null
                        hoveredIndex = null
                        if (commit && item != null && target != null && target != index) {
                            onReorder(item, items.getOrNull(target))
                        }
                    }

                    // The gesture and measurement wrapper is never transformed, so
                    // pointer positions keep mapping to stable window coordinates.
                    // Only the content inside it moves while being dragged.
                    Box(
                        modifier = Modifier
                            .width(APP_ICON_WIDTH)
                            .then(if (isDraggingThis) Modifier.zIndex(1f) else Modifier)
                            .onGloballyPositioned { coordinates ->
                                slotBounds[index] = coordinates.boundsInWindow()
                                slotCoordinates[index] = coordinates
                            }
                            .then(
                                if (item == null) {
                                    // Empty holes stay transparent to gestures so a long
                                    // press there still reaches the app-list settings menu.
                                    Modifier
                                } else {
                                    Modifier.dragReorderable(
                                        tileCoordinates = { slotCoordinates[index] },
                                        onTap = {
                                            when (item) {
                                                is HomeGridItem.AppItem -> onAppClick(item.app)
                                                is HomeGridItem.FolderItem -> onFolderClick(item.folder)
                                            }
                                        },
                                        onLongClick = {
                                            when (item) {
                                                is HomeGridItem.AppItem -> onAppLongClick(item.app)
                                                is HomeGridItem.FolderItem -> onFolderLongClick(item.folder)
                                            }
                                        },
                                        onPickUp = pickUp,
                                        onDragMove = moveDrag,
                                        onDragEnd = endDrag,
                                        onPressChange = { isDown -> pressedIndex = if (isDown) index else null },
                                    )
                                },
                            ),
                    ) {
                        val contentModifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isDraggingThis) {
                                    Modifier.graphicsLayer {
                                        val origin = slotBounds[index]?.center ?: dragPositionWindow
                                        translationX = dragPositionWindow.x - origin.x
                                        translationY = dragPositionWindow.y - origin.y
                                        scaleX = DRAG_SCALE
                                        scaleY = DRAG_SCALE
                                    }
                                } else {
                                    Modifier
                                },
                            )
                            .then(
                                if (isHovered) Modifier.border(1.dp, Color.Gray, APP_ICON_CORNER_SHAPE) else Modifier,
                            )

                        when (item) {
                            is HomeGridItem.AppItem -> AppIconTile(app = item.app, modifier = contentModifier)
                            is HomeGridItem.FolderItem -> FolderKnobTile(
                                folder = item.folder,
                                pressed = pressedIndex == index,
                                modifier = contentModifier,
                            )
                            null -> EmptyHoleTile(modifier = contentModifier)
                        }
                    }
                }
            }
        }
    }
}

private const val DRAG_SCALE = 1.1f

@Composable
private fun EmptyHoleTile(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(1.dp, Color.Gray, CircleShape),
    )
}

@Composable
private fun FolderKnobTile(
    folder: AppFolder,
    pressed: Boolean,
    modifier: Modifier = Modifier,
) {
    RotaryKnob(
        angleDeg = 0f,
        label = folder.name,
        onTap = {},
        onLongPress = {},
        diameter = APP_ICON_WIDTH,
        clickable = false,
        pressed = pressed,
        modifier = modifier,
    )
}

private val APP_ICON_CORNER_SHAPE = RoundedCornerShape(6.dp)
private val APP_ICON_BACKGROUND_COLOR = NothingGrays.Base

@Composable
private fun AppIconTile(
    app: AppInfo,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(APP_ICON_BACKGROUND_COLOR, APP_ICON_CORNER_SHAPE),
        ) {
            AppIconDot(modifier = Modifier.align(Alignment.TopEnd))
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
