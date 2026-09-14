package com.sekitakumi.nothingfoldlauncher.ui

import com.sekitakumi.nothingfoldlauncher.data.AppInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AlphabetIndexTest {

    @Test
    fun `hides index below threshold`() {
        val apps = (1..19).map { AppInfo(label = "App$it", packageName = "pkg$it") }
        assertEquals(false, shouldShowAlphabetIndex(apps))
    }

    @Test
    fun `shows index at or above threshold`() {
        val apps = (1..20).map { AppInfo(label = "App$it", packageName = "pkg$it") }
        assertEquals(true, shouldShowAlphabetIndex(apps))
    }

    @Test
    fun `buckets uppercase letter labels to themselves`() {
        assertEquals('A', bucketForLabel("Amazon"))
        assertEquals('C', bucketForLabel("chrome"))
    }

    @Test
    fun `buckets non A-Z leading characters to hash`() {
        assertEquals('#', bucketForLabel("3M"))
        assertEquals('#', bucketForLabel("アプリ"))
        assertEquals('#', bucketForLabel(""))
    }

    @Test
    fun `builds first-index map per bucket in list order`() {
        val apps = listOf(
            AppInfo("3M", "p0"),
            AppInfo("Amazon", "p1"),
            AppInfo("Apple", "p2"),
            AppInfo("Chrome", "p3"),
            AppInfo("Discord", "p4"),
        )
        val map = letterIndexMap(apps)
        assertEquals(0, map['#'])
        assertEquals(1, map['A'])
        assertEquals(3, map['C'])
        assertEquals(4, map['D'])
        assertEquals(null, map['B'])
    }

    @Test
    fun `hash bucket points to trailing non-alphabetic group when both leading and trailing groups exist`() {
        // Sorted order (as AppRepository would produce via label.lowercase()):
        // digit-prefixed labels sort before letters, non-Latin labels sort after letters,
        // so the '#' bucket exists both before 'A' and after 'Z' in the real app list.
        val apps = listOf(
            AppInfo("1Password", "p0"),
            AppInfo("Amazon", "p1"),
            AppInfo("Chrome", "p2"),
            AppInfo("ライブ天気", "p3"),
            AppInfo("天気を知る", "p4"),
        )
        val map = letterIndexMap(apps)
        assertEquals(3, map['#'])
    }

    @Test
    fun `scroll index returns exact match when letter present`() {
        val map = mapOf('A' to 0, 'C' to 3)
        assertEquals(0, scrollIndexForLetter('A', map, totalItems = 5))
    }

    @Test
    fun `scroll index falls back to nearest later letter when missing`() {
        val map = mapOf('A' to 0, 'C' to 3)
        assertEquals(3, scrollIndexForLetter('B', map, totalItems = 5))
    }

    @Test
    fun `scroll index falls back to last item when nothing later exists`() {
        val map = mapOf('A' to 0, 'C' to 3)
        assertEquals(4, scrollIndexForLetter('D', map, totalItems = 5))
    }

    @Test
    fun `scroll index returns null for empty list`() {
        assertNull(scrollIndexForLetter('A', emptyMap(), totalItems = 0))
    }

    @Test
    fun `bar position maps start and end to first and last letters`() {
        assertEquals('A', letterForBarPosition(0f))
        assertEquals('#', letterForBarPosition(1f))
    }

    @Test
    fun `bar position clamps out of range values`() {
        assertEquals('A', letterForBarPosition(-5f))
        assertEquals('#', letterForBarPosition(5f))
    }

    @Test
    fun `alphabetIndexLetterScale peaks at the touched letter`() {
        assertEquals(ALPHABET_INDEX_MAX_SCALE, alphabetIndexLetterScale(distance = 0), 0.001f)
    }

    @Test
    fun `alphabetIndexLetterScale falls off with distance and settles at 1`() {
        val atOne = alphabetIndexLetterScale(distance = 1)
        val atZero = alphabetIndexLetterScale(distance = 0)
        assertTrue(atOne < atZero)
        assertTrue(atOne > 1f)
        assertEquals(1f, alphabetIndexLetterScale(distance = ALPHABET_INDEX_MAGNIFY_RADIUS), 0.001f)
        assertEquals(1f, alphabetIndexLetterScale(distance = 100), 0.001f)
    }

    @Test
    fun `drag offset starts at the base offset when finger has not moved`() {
        assertEquals(-40f, alphabetIndexDragOffsetX(downX = 100f, currentX = 100f, baseOffsetPx = -40f), 0.001f)
    }

    @Test
    fun `drag offset follows further leftward movement beyond the base`() {
        assertEquals(-80f, alphabetIndexDragOffsetX(downX = 100f, currentX = 60f, baseOffsetPx = -40f), 0.001f)
    }

    @Test
    fun `drag offset eases back toward zero with rightward movement`() {
        assertEquals(-20f, alphabetIndexDragOffsetX(downX = 100f, currentX = 120f, baseOffsetPx = -40f), 0.001f)
    }

    @Test
    fun `drag offset caps at zero even past the bar`() {
        assertEquals(0f, alphabetIndexDragOffsetX(downX = 100f, currentX = 200f, baseOffsetPx = -40f), 0.001f)
    }
}
