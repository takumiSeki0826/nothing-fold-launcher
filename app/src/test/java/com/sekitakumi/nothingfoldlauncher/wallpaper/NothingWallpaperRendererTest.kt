package com.sekitakumi.nothingfoldlauncher.wallpaper

import org.junit.Assert.assertEquals
import org.junit.Test

class NothingWallpaperRendererTest {

    @Test
    fun `positions card below the time and date rows, scaled from width`() {
        val rect = calculateCardRect(
            width = 1080f,
            height = 2400f,
            timeAscent = -150f,
            timeDescent = 40f,
        )

        assertEquals(64f, rect.left, 0.01f)
        assertEquals(834f, rect.top, 0.01f)
        assertEquals(1016f, rect.right, 0.01f)
        assertEquals(1554f, rect.bottom, 0.01f)
    }

    @Test
    fun `clamps card to bottom margin on short screens instead of overflowing`() {
        val rect = calculateCardRect(
            width = 1080f,
            height = 800f,
            timeAscent = -150f,
            timeDescent = 40f,
        )

        assertEquals(440f, rect.top, 0.01f)
        assertEquals(680f, rect.bottom, 0.01f)
    }
}
