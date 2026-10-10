package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NowPlayingSessionFilterTest {

    private data class S(val pkg: String, val playing: Boolean = false)

    private fun pick(vararg s: S) = pickNowPlayingSession(s.toList(), { it.pkg }, { it.playing })

    @Test
    fun `skips kindle and picks the next session`() {
        assertEquals(S("com.spotify.music"), pick(S("com.amazon.kindle"), S("com.spotify.music")))
    }

    @Test
    fun `returns null when only kindle is active`() {
        assertNull(pick(S("com.amazon.kindle", playing = true)))
    }

    @Test
    fun `keeps the first session when none are playing`() {
        assertEquals(S("a"), pick(S("a"), S("b")))
    }

    @Test
    fun `prefers a playing session over a paused one at the top`() {
        assertEquals(S("music", true), pick(S("youtube"), S("music", true)))
    }

    @Test
    fun `playing kindle never wins over a paused session`() {
        assertEquals(S("youtube"), pick(S("com.amazon.kindle", true), S("youtube")))
    }

    @Test
    fun `returns null for empty or null list`() {
        assertNull(pick())
        assertNull(pickNowPlayingSession<S>(null, { it.pkg }, { it.playing }))
    }
}
