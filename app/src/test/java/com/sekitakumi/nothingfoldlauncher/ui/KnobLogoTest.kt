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
}
