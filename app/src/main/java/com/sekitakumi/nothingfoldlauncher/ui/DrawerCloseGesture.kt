package com.sekitakumi.nothingfoldlauncher.ui

import com.sekitakumi.nothingfoldlauncher.data.AppInfo

// Swipe-down-to-close for the app drawer, as a plain object so the accumulation
// rules stay testable.
//
// The accumulator only ever grows while the list is pinned to its top and the
// drag keeps pulling downward; anything else zeroes it. Crucially that includes
// lifting the finger ([onGestureEnd]): without it, several separate small tugs
// at the top of the list add up and close the drawer out of nowhere - which is
// easy to hit when a search matches nothing and the list can't scroll at all.
class DrawerCloseGesture(private val thresholdPx: Float) {

    private var accumulatedPx = 0f

    // Returns true when this scroll completes a close gesture.
    fun onOverscroll(availableY: Float, atTop: Boolean): Boolean {
        if (!atTop || availableY <= 0f) {
            accumulatedPx = 0f
            return false
        }
        accumulatedPx += availableY
        if (accumulatedPx <= thresholdPx) return false
        accumulatedPx = 0f
        return true
    }

    fun onGestureEnd() {
        accumulatedPx = 0f
    }
}

// A search that matched nothing needs to say so: an empty LazyColumn renders as
// bare black, which reads as a frozen screen. An empty query means the app list
// simply hasn't loaded yet, so stay quiet there.
fun shouldShowEmptyState(apps: List<AppInfo>, query: String): Boolean =
    apps.isEmpty() && query.isNotBlank()
