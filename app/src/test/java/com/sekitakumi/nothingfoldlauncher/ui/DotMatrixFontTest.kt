package com.sekitakumi.nothingfoldlauncher.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DotMatrixFontTest {

    private val supported = ('0'..'9') + ('A'..'Z') + "°%.,-:/ #+()↑↓?…!&'\";*@_=<>[]{}~|\\^\$`".toList()

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

    @Test
    fun `typographic variants reuse the ascii glyph`() {
        assertEquals(dotMatrixGlyph('\''), dotMatrixGlyph('’'))
        assertEquals(dotMatrixGlyph('\''), dotMatrixGlyph('‘'))
        assertEquals(dotMatrixGlyph('"'), dotMatrixGlyph('“'))
        assertEquals(dotMatrixGlyph('"'), dotMatrixGlyph('”'))
        assertEquals(dotMatrixGlyph('-'), dotMatrixGlyph('–'))
        assertEquals(dotMatrixGlyph('-'), dotMatrixGlyph('—'))
    }

    @Test
    fun `fullwidth ascii reuses the ascii glyph`() {
        assertEquals(dotMatrixGlyph('!'), dotMatrixGlyph('！'))
        assertEquals(dotMatrixGlyph('?'), dotMatrixGlyph('？'))
        assertEquals(dotMatrixGlyph('&'), dotMatrixGlyph('＆'))
        assertEquals(dotMatrixGlyph('A'), dotMatrixGlyph('Ａ'))
        assertEquals(dotMatrixGlyph('5'), dotMatrixGlyph('５'))
        assertEquals(dotMatrixGlyph(' '), dotMatrixGlyph('　'))
    }

    @Test
    fun `punctuation glyphs may be narrower than letters`() {
        assertEquals(1, dotMatrixGlyph('!')[0].length)
        assertEquals(5, dotMatrixGlyph('A')[0].length)
    }
}
