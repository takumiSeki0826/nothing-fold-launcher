package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class NowPlayingTextTest {

    @Test
    fun `plain latin text is unchanged`() {
        assertEquals("Galileo Galilei", nowPlayingDisplayText("Galileo Galilei"))
        assertEquals("Wind - Akeboshi", nowPlayingDisplayText("Wind - Akeboshi"))
    }

    @Test
    fun `prefers the latin segment after a dash`() {
        assertEquals(
            "Four o'clock (feat. AKARIAKARI)",
            nowPlayingDisplayText("四時にはね。 - Four o'clock (feat. AKARIAKARI)"),
        )
        assertEquals(
            "Sayonara ha Emotion",
            nowPlayingDisplayText("さよならはエモーション - Sayonara ha Emotion"),
        )
    }

    @Test
    fun `picks the first latin segment when there are several`() {
        assertEquals("One", nowPlayingDisplayText("曲 - One - Two"))
    }

    @Test
    fun `uses a leading latin segment too`() {
        assertEquals("Galileo Galilei", nowPlayingDisplayText("Galileo Galilei - 嵐の前"))
    }

    @Test
    fun `ignores segments that still contain japanese`() {
        assertEquals("Hello", nowPlayingDisplayText("あ - い - Hello"))
    }

    @Test
    fun `ignores segments without any letter`() {
        assertEquals("kyoku - 123", nowPlayingDisplayText("きょく - 123"))
    }

    @Test
    fun `falls back to kana romanization when no latin segment exists`() {
        assertEquals("hitsujibungaku", nowPlayingDisplayText("ヒツジブンガク"))
    }

    @Test
    fun `keeps kanji when only romanization is possible`() {
        assertEquals("声", nowPlayingDisplayText("声"))
    }

    @Test
    fun `empty stays empty`() {
        assertEquals("", nowPlayingDisplayText(""))
    }
}
