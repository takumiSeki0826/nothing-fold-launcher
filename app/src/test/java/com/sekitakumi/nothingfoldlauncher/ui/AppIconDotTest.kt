package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class AppIconDotTest {

    @Test
    fun `scaledDurationMs at max drop distance returns max duration`() {
        assertEquals(260, scaledDurationMs(distanceDp = 44f, maxDurationMs = 260))
    }

    @Test
    fun `scaledDurationMs scales down with shorter distance`() {
        assertEquals(130, scaledDurationMs(distanceDp = 11f, maxDurationMs = 260))
    }

    @Test
    fun `scaledDurationMs clamps to floor for very short distance`() {
        assertEquals(60, scaledDurationMs(distanceDp = 0f, maxDurationMs = 260))
    }

    @Test
    fun `scaledDurationMs clamps distance beyond max to max duration`() {
        assertEquals(260, scaledDurationMs(distanceDp = 100f, maxDurationMs = 260))
    }
}
