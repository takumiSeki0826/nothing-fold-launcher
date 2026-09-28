package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import kotlinx.coroutines.withTimeoutOrNull

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
//
// The callbacks are read through [rememberUpdatedState] because `pointerInput` keeps
// running the block it was first composed with. Without it a tile would keep calling
// callbacks that captured old grid contents, so a tap after reorder could act on the
// previous order.
@Composable
internal fun Modifier.dragReorderable(
    tileCoordinates: () -> LayoutCoordinates?,
    onTap: () -> Unit,
    onLongClick: () -> Unit,
    onPickUp: () -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: (commit: Boolean) -> Unit,
    onPressChange: (Boolean) -> Unit = {},
): Modifier {
    val currentTileCoordinates by rememberUpdatedState(tileCoordinates)
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnLongClick by rememberUpdatedState(onLongClick)
    val currentOnPickUp by rememberUpdatedState(onPickUp)
    val currentOnDragMove by rememberUpdatedState(onDragMove)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnPressChange by rememberUpdatedState(onPressChange)

    return this.pointerInput(Unit) {
        val longPressTimeoutMillis = viewConfiguration.longPressTimeoutMillis
        val touchSlop = viewConfiguration.touchSlop
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            // Claim the tap/long-press role so the grid's own "long-press empty space"
            // handler does not fire on top of a tile. Ancestor drag detectors use
            // requireUnconsumed = false, so swipes still see this down.
            down.consume()
            currentOnPressChange(true)

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
                    currentOnPressChange(false)
                    currentOnTap()
                }
                slippedAway -> currentOnPressChange(false)
                else -> {
                    currentOnPressChange(false)
                    currentOnPickUp()
                    var moved = false
                    while (true) {
                        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
                        if (change == null) {
                            currentOnDragEnd(false)
                            break
                        }
                        if (!change.pressed) {
                            currentOnDragEnd(moved)
                            if (!moved) currentOnLongClick()
                            break
                        }
                        if (!moved && (change.position - down.position).getDistance() > touchSlop) {
                            moved = true
                        }
                        if (moved) {
                            currentTileCoordinates()?.localToWindow(change.position)?.let(currentOnDragMove)
                        }
                        change.consume()
                    }
                }
            }
        }
    }
}
