package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DotMatrixFontTest {

    private val supported = ('0'..'9') + ('A'..'Z') + "°%.,-:/ #+()↑↓?".toList()

    @Test
    fun `every supported glyph has 7 rows of equal width`() {
        supported.forEach { c ->
            val glyph = dotMatrixGlyph(c)
            assertEquals("rows of '$c'", 7, glyph.size)
            assertTrue("width of '$c'", glyph.all { it.length == glyph[0].length })
            assertTrue("chars of '$c'", glyph.all { row -> row.all { it == '#' || it == '.' } })
        }
    }

    @Test
    fun `every supported char has a glyph`() {
        supported.forEach { assertTrue("'$it'", hasDotMatrixGlyph(it)) }
    }

    @Test
    fun `lowercase ascii letters reuse the uppercase glyph`() {
        ('a'..'z').forEach { c ->
            assertTrue("'$c'", hasDotMatrixGlyph(c))
            assertEquals(dotMatrixGlyph(c.uppercaseChar()), dotMatrixGlyph(c))
        }
    }

    @Test
    fun `chars outside the font have no glyph`() {
        assertFalse(hasDotMatrixGlyph('あ'))
        assertFalse(hasDotMatrixGlyph('★'))
    }
}
