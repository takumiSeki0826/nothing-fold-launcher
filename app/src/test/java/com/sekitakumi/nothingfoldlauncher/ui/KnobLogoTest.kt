package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KnobLogoTest {

    @Test
    fun `youtube package maps to the play logo`() {
        assertEquals(KnobLogo.YOUTUBE, knobLogoFor("com.google.android.youtube"))
    }

    @Test
    fun `kindle package maps to the kindle logo`() {
        assertEquals(KnobLogo.KINDLE, knobLogoFor("com.amazon.kindle"))
    }

    @Test
    fun `youtube music and unassigned knobs have no logo`() {
        assertNull(knobLogoFor("com.google.android.apps.youtube.music"))
        assertNull(knobLogoFor("com.sec.android.app.myfiles"))
        assertNull(knobLogoFor("com.twitter.android"))
        assertNull(knobLogoFor("com.anthropic.claude"))
        assertNull(knobLogoFor(null))
    }

    @Test
    fun `patterns are rectangular`() {
        for (logo in KnobLogo.values()) {
            val rows = logo.pattern
            assertTrue(rows.all { it.length == rows.first().length })
        }
    }

    @Test
    fun `kindle is drawn as the word KINDLE in the dot-matrix font`() {
        assertEquals(listOf("KINDLE"), KnobLogo.KINDLE.wordmark)
        assertTrue(KnobLogo.KINDLE.pattern.isEmpty())
        assertTrue(KnobLogo.KINDLE.wordmark.all { line -> line.all(::hasDotMatrixGlyph) })
    }

    @Test
    fun `every logo has either a pattern or a wordmark`() {
        for (logo in KnobLogo.values()) {
            assertTrue(logo.pattern.isNotEmpty() || logo.wordmark.isNotEmpty())
        }
    }
}
