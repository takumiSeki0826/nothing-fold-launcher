package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import kotlinx.coroutines.withTimeoutOrNull

fun nearestSlotIndex(position: Offset, slotBounds: List<Rect?>): Int? {
    var bestIndex: Int? = null
    var bestDistanceSq = Float.MAX_VALUE
    slotBounds.forEachIndexed { index, rect ->
        if (rect == null) return@forEachIndexed
        val dx = rect.center.x - position.x
        val dy = rect.center.y - position.y
        val distanceSq = dx * dx + dy * dy
        if (distanceSq < bestDistanceSq) {
            bestDistanceSq = distanceSq
            bestIndex = index
        }
    }
    return bestIndex
}

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
internal fun Modifier.dragReorderable(
    tileCoordinates: () -> LayoutCoordinates?,
    onTap: () -> Unit,
    onLongClick: () -> Unit,
    onPickUp: () -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: (commit: Boolean) -> Unit,
    onPressChange: (Boolean) -> Unit = {},
): Modifier = this.pointerInput(Unit) {
    val longPressTimeoutMillis = viewConfiguration.longPressTimeoutMillis
    val touchSlop = viewConfiguration.touchSlop
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        // Claim the tap/long-press role so the grid's own "long-press empty space"
        // handler does not fire on top of a tile. Ancestor drag detectors use
        // requireUnconsumed = false, so swipes still see this down.
        down.consume()
        onPressChange(true)

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
            lifted -> {
                onPressChange(false)
                onTap()
            }
            slippedAway -> onPressChange(false)
            else -> {
                onPressChange(false)
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
