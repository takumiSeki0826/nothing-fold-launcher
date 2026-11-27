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
    fun `x package maps to the cross logo`() {
        assertEquals(KnobLogo.X, knobLogoFor("com.twitter.android"))
    }

    @Test
    fun `youtube music and unassigned knobs have no logo`() {
        assertNull(knobLogoFor("com.google.android.apps.youtube.music"))
        assertNull(knobLogoFor("com.sec.android.app.myfiles"))
        assertNull(knobLogoFor(null))
    }

    @Test
    fun `patterns are rectangular`() {
        for (logo in KnobLogo.values()) {
            val rows = knobLogoPattern(logo)
            assertTrue(rows.all { it.length == rows.first().length })
        }
    }
}
