package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import kotlinx.coroutines.withTimeoutOrNull
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
    onReorder: (from: HomeGridItem, to: HomeGridItem?) -> Unit,
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
                            slotCount = null,
                            iconColorFor = iconColorFor,
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
                        items = items.take(coverGridMaxItems()),
                        columns = gridColumns,
                        slotCount = coverGridMaxItems(),
                        iconColorFor = iconColorFor,
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
    slotCount: Int?,
    iconColorFor: (AppInfo) -> IconPaletteColor?,
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
                            is HomeGridItem.FolderItem -> FolderKnobTile(folder = item.folder, modifier = contentModifier)
                            null -> EmptyHoleTile(modifier = contentModifier)
                        }
                    }
                }
            }
        }
    }
}

private const val DRAG_SCALE = 1.1f

// Tap, long-press and drag-to-reorder from a single gesture, replacing
// `combinedClickable` on grid tiles: a drag that only starts after a long press
// cannot be layered on top of `combinedClickable`, since both would compete for
// the same pointer events.
//
// Released before the long-press timeout -> [onTap]. Held still past it -> the tile
// is picked up ([onPickUp]) and from then on follows the finger; releasing without
// moving is a plain long-press ([onLongClick]), releasing after moving commits the
// reorder. A swipe that starts on a tile belongs to the home screen, so the gesture
// bows out once it passes touch slop before the pickup.
private fun Modifier.dragReorderable(
    tileCoordinates: () -> LayoutCoordinates?,
    onTap: () -> Unit,
    onLongClick: () -> Unit,
    onPickUp: () -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: (commit: Boolean) -> Unit,
): Modifier = this.pointerInput(Unit) {
    val longPressTimeoutMillis = viewConfiguration.longPressTimeoutMillis
    val touchSlop = viewConfiguration.touchSlop
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        // Claim the tap/long-press role so the grid's own "long-press empty space"
        // handler does not fire on top of a tile. Ancestor drag detectors use
        // requireUnconsumed = false, so swipes still see this down.
        down.consume()

        var lifted = false
        var slippedAway = false
        // Stay passive until the long press fires: consuming moves here would
        // starve the home screen's swipe detector.
        withTimeoutOrNull(longPressTimeoutMillis) {
            while (true) {
                val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
                if (change == null) {
                    slippedAway = true
                    return@withTimeoutOrNull
                }
                if (!change.pressed) {
                    lifted = true
                    return@withTimeoutOrNull
                }
                if ((change.position - down.position).getDistance() > touchSlop) {
                    slippedAway = true
                    return@withTimeoutOrNull
                }
            }
            @Suppress("UNREACHABLE_CODE") Unit
        }

        when {
            lifted -> onTap()
            slippedAway -> Unit
            else -> {
                onPickUp()
                var moved = false
                while (true) {
                    val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
                    if (change == null) {
                        onDragEnd(false)
                        break
                    }
                    if (!change.pressed) {
                        onDragEnd(moved)
                        if (!moved) onLongClick()
                        break
                    }
                    if (!moved && (change.position - down.position).getDistance() > touchSlop) {
                        moved = true
                    }
                    if (moved) {
                        tileCoordinates()?.localToWindow(change.position)?.let(onDragMove)
                    }
                    change.consume()
                }
            }
        }
    }
}

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
    modifier: Modifier = Modifier,
) {
    RotaryKnob(
        angleDeg = 0f,
        label = folder.name,
        onTap = {},
        onLongPress = {},
        diameter = APP_ICON_WIDTH,
        clickable = false,
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
