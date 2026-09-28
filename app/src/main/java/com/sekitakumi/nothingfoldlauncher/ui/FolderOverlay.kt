package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.sekitakumi.nothingfoldlauncher.data.AppInfo
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingGrays

private val FOLDER_OVERLAY_ICON_WIDTH = 60.dp
private val FOLDER_OVERLAY_COLUMNS = 4
private const val DRAG_SCALE = 1.1f

@Composable
fun FolderOverlay(
    name: String,
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onReorder: (from: AppInfo, to: AppInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        // dismissOnClickOutside relies on the platform's outside-touch detection, which also
        // fires while a drag inside the dialog strays past its window bounds, closing it
        // mid-reorder. Handling the outside tap ourselves (below) avoids that false trigger.
        properties = DialogProperties(dismissOnClickOutside = false, usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    )
                    .background(Color.Black, RoundedCornerShape(12.dp))
                    .padding(20.dp),
            ) {
                Text(text = name, color = Color.White, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(12.dp))
                if (apps.isEmpty()) {
                    Text(text = "No apps yet", color = Color.Gray)
                } else {
                    FolderOverlayGrid(apps = apps, onAppClick = onAppClick, onReorder = onReorder)
                }
            }
        }
    }
}

@Composable
private fun FolderOverlayGrid(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onReorder: (from: AppInfo, to: AppInfo) -> Unit,
) {
    val slotBounds = remember { mutableStateMapOf<Int, Rect>() }
    val slotCoordinates = remember { mutableStateMapOf<Int, LayoutCoordinates>() }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragPositionWindow by remember { mutableStateOf(Offset.Zero) }
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    val haptics = LocalHapticFeedback.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        apps.chunked(FOLDER_OVERLAY_COLUMNS).forEachIndexed { rowIndex, rowApps ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowApps.forEachIndexed { columnIndex, app ->
                    val index = rowIndex * FOLDER_OVERLAY_COLUMNS + columnIndex
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
                        hoveredIndex = nearestSlotIndex(windowPosition, apps.indices.map { slotBounds[it] })
                    }
                    val endDrag: (Boolean) -> Unit = { commit ->
                        val target = hoveredIndex
                        draggingIndex = null
                        hoveredIndex = null
                        if (commit && target != null && target != index) {
                            apps.getOrNull(target)?.let { onReorder(app, it) }
                        }
                    }

                    FolderOverlayAppTile(
                        app = app,
                        isDragging = isDraggingThis,
                        isHovered = isHovered,
                        dragPositionWindow = dragPositionWindow,
                        slotCenter = { slotBounds[index]?.center },
                        modifier = Modifier
                            .width(FOLDER_OVERLAY_ICON_WIDTH)
                            .then(if (isDraggingThis) Modifier.zIndex(1f) else Modifier)
                            .onGloballyPositioned { coordinates ->
                                slotBounds[index] = coordinates.boundsInWindow()
                                slotCoordinates[index] = coordinates
                            }
                            .dragReorderable(
                                tileCoordinates = { slotCoordinates[index] },
                                onTap = { onAppClick(app) },
                                onLongClick = {},
                                onPickUp = pickUp,
                                onDragMove = moveDrag,
                                onDragEnd = endDrag,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderOverlayAppTile(
    app: AppInfo,
    isDragging: Boolean,
    isHovered: Boolean,
    dragPositionWindow: Offset,
    slotCenter: () -> Offset?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .then(
                    if (isDragging) {
                        Modifier.graphicsLayer {
                            val origin = slotCenter() ?: dragPositionWindow
                            translationX = dragPositionWindow.x - origin.x
                            translationY = dragPositionWindow.y - origin.y
                            scaleX = DRAG_SCALE
                            scaleY = DRAG_SCALE
                        }
                    } else {
                        Modifier
                    },
                )
                .background(NothingGrays.Base, RoundedCornerShape(6.dp))
                .then(if (isHovered) Modifier.border(1.dp, Color.Gray, RoundedCornerShape(6.dp)) else Modifier),
        ) {
            AppIconDot(modifier = Modifier.align(Alignment.TopEnd))
        }
        Text(
            text = app.label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}
