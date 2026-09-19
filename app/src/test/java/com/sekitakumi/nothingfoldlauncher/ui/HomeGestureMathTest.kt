package com.sekitakumi.nothingfoldlauncher.ui

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeGestureMathTest {

    @Test
    fun `closes drawer when home gesture intent arrives while drawer is open`() {
        assertTrue(shouldCloseDrawerOnNewIntent(intentAction = Intent.ACTION_MAIN, isDrawerOpen = true))
    }

    @Test
    fun `does nothing when drawer is already closed`() {
        assertFalse(shouldCloseDrawerOnNewIntent(intentAction = Intent.ACTION_MAIN, isDrawerOpen = false))
    }

    @Test
    fun `ignores non-home intents even when drawer is open`() {
        assertFalse(shouldCloseDrawerOnNewIntent(intentAction = Intent.ACTION_VIEW, isDrawerOpen = true))
    }

    @Test
    fun `ignores null action`() {
        assertFalse(shouldCloseDrawerOnNewIntent(intentAction = null, isDrawerOpen = true))
    }

    @Test
    fun `swipe up from home opens drawer`() {
        val route = nextHomeRoute(HomeRoute.HOME, dragAccumX = 0f, dragAccumY = -150f)
        assertEquals(HomeRoute.DRAWER, route)
    }

    @Test
    fun `swipe left from home stays on home`() {
        val route = nextHomeRoute(HomeRoute.HOME, dragAccumX = -150f, dragAccumY = 0f)
        assertEquals(HomeRoute.HOME, route)
    }

    @Test
    fun `small drag from home stays on home`() {
        val route = nextHomeRoute(HomeRoute.HOME, dragAccumX = -50f, dragAccumY = -50f)
        assertEquals(HomeRoute.HOME, route)
    }

    @Test
    fun `swipe left or down from drawer returns home`() {
        assertEquals(HomeRoute.HOME, nextHomeRoute(HomeRoute.DRAWER, dragAccumX = -150f, dragAccumY = 0f))
        assertEquals(HomeRoute.HOME, nextHomeRoute(HomeRoute.DRAWER, dragAccumX = 0f, dragAccumY = 150f))
    }

    @Test
    fun `small drag from drawer stays on drawer`() {
        val route = nextHomeRoute(HomeRoute.DRAWER, dragAccumX = -50f, dragAccumY = 50f)
        assertEquals(HomeRoute.DRAWER, route)
    }
}
