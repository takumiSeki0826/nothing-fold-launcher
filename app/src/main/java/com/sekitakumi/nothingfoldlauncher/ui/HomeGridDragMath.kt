package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

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
