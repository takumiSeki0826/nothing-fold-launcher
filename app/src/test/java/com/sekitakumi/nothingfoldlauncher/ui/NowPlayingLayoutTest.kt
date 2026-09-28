package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class NowPlayingLayoutTest {

    @Test
    fun `header height is the control min height when no control is shown`() {
        assertEquals(48, nowPlayingHeaderHeightDp(textHeightDp = 18, hasControl = false))
    }

    @Test
    fun `header height matches whether or not the control is shown`() {
        val withoutControl = nowPlayingHeaderHeightDp(textHeightDp = 18, hasControl = false)
        val withControl = nowPlayingHeaderHeightDp(textHeightDp = 18, hasControl = true)
        assertEquals(withControl, withoutControl)
    }

    @Test
    fun `header height grows to fit text taller than the control`() {
        assertEquals(60, nowPlayingHeaderHeightDp(textHeightDp = 60, hasControl = false))
    }
}
