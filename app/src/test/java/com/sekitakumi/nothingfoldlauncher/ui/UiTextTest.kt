package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class UiTextTest {

    @Test
    fun `appCountLabel uses plural for zero`() {
        assertEquals("0 apps", appCountLabel(0))
    }

    @Test
    fun `appCountLabel uses singular for one`() {
        assertEquals("1 app", appCountLabel(1))
    }

    @Test
    fun `appCountLabel uses plural for many`() {
        assertEquals("2 apps", appCountLabel(2))
    }
}
