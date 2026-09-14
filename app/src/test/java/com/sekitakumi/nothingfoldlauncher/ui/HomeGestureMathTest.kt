package com.sekitakumi.nothingfoldlauncher.ui

import android.content.Intent
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
}
