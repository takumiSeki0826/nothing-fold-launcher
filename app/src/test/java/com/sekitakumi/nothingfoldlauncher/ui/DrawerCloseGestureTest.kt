package com.sekitakumi.nothingfoldlauncher.ui

import com.sekitakumi.nothingfoldlauncher.data.AppInfo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val THRESHOLD = 100f

class DrawerCloseGestureTest {

    @Test
    fun `closes once the downward overscroll at the top passes the threshold`() {
        val gesture = DrawerCloseGesture(thresholdPx = THRESHOLD)
        assertFalse(gesture.onOverscroll(availableY = 60f, atTop = true))
        assertTrue(gesture.onOverscroll(availableY = 60f, atTop = true))
    }

    @Test
    fun `does not close while the overscroll stays under the threshold`() {
        val gesture = DrawerCloseGesture(thresholdPx = THRESHOLD)
        assertFalse(gesture.onOverscroll(availableY = 30f, atTop = true))
        assertFalse(gesture.onOverscroll(availableY = 30f, atTop = true))
        assertFalse(gesture.onOverscroll(availableY = 30f, atTop = true))
    }

    // The bug: two separate small tugs at the top of the list used to add up and
    // close the drawer, because the accumulator only reset on a scroll that moved
    // the list. Lifting the finger has to clear it.
    @Test
    fun `forgets the overscroll accumulated by a finished gesture`() {
        val gesture = DrawerCloseGesture(thresholdPx = THRESHOLD)
        assertFalse(gesture.onOverscroll(availableY = 60f, atTop = true))

        gesture.onGestureEnd()

        assertFalse(gesture.onOverscroll(availableY = 60f, atTop = true))
    }

    @Test
    fun `forgets the overscroll once the list scrolls away from the top`() {
        val gesture = DrawerCloseGesture(thresholdPx = THRESHOLD)
        assertFalse(gesture.onOverscroll(availableY = 60f, atTop = true))
        assertFalse(gesture.onOverscroll(availableY = 60f, atTop = false))
        assertFalse(gesture.onOverscroll(availableY = 60f, atTop = true))
    }

    @Test
    fun `forgets the overscroll on an upward scroll`() {
        val gesture = DrawerCloseGesture(thresholdPx = THRESHOLD)
        assertFalse(gesture.onOverscroll(availableY = 60f, atTop = true))
        assertFalse(gesture.onOverscroll(availableY = -10f, atTop = true))
        assertFalse(gesture.onOverscroll(availableY = 60f, atTop = true))
    }

    @Test
    fun `starts over after closing so the reopened drawer needs a full swipe`() {
        val gesture = DrawerCloseGesture(thresholdPx = THRESHOLD)
        assertFalse(gesture.onOverscroll(availableY = 60f, atTop = true))
        assertTrue(gesture.onOverscroll(availableY = 60f, atTop = true))

        assertFalse(gesture.onOverscroll(availableY = 60f, atTop = true))
    }
}

class DrawerEmptyStateTest {

    private fun app(label: String) = AppInfo(label = label, packageName = "pkg.$label")

    @Test
    fun `shows the empty state when a search matches nothing`() {
        assertTrue(shouldShowEmptyState(apps = emptyList(), query = "zzz"))
    }

    @Test
    fun `stays quiet on an empty query while the app list is still loading`() {
        assertFalse(shouldShowEmptyState(apps = emptyList(), query = ""))
    }

    @Test
    fun `stays quiet on a blank query typed as whitespace`() {
        assertFalse(shouldShowEmptyState(apps = emptyList(), query = "   "))
    }

    @Test
    fun `stays quiet when the search has results`() {
        assertFalse(shouldShowEmptyState(apps = listOf(app("Chrome")), query = "ch"))
    }
}
