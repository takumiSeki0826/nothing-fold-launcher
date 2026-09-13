package com.sekitakumi.nothingfoldlauncher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AppVisibilityTest {

    @Test
    fun `toggleFavorite adds a package not yet favorited`() {
        val (favorites, hidden) = toggleFavorite(favorites = emptySet(), hidden = emptySet(), packageName = "a")
        assertEquals(setOf("a"), favorites)
        assertEquals(emptySet<String>(), hidden)
    }

    @Test
    fun `toggleFavorite removes a package already favorited`() {
        val (favorites, hidden) = toggleFavorite(favorites = setOf("a"), hidden = emptySet(), packageName = "a")
        assertEquals(emptySet<String>(), favorites)
        assertEquals(emptySet<String>(), hidden)
    }

    @Test
    fun `toggleFavorite unhides a package when adding it as favorite`() {
        val (favorites, hidden) = toggleFavorite(favorites = emptySet(), hidden = setOf("a"), packageName = "a")
        assertEquals(setOf("a"), favorites)
        assertEquals(emptySet<String>(), hidden)
    }

    @Test
    fun `toggleHidden hides a package not yet hidden`() {
        val (favorites, hidden) = toggleHidden(favorites = emptySet(), hidden = emptySet(), packageName = "a")
        assertEquals(emptySet<String>(), favorites)
        assertEquals(setOf("a"), hidden)
    }

    @Test
    fun `toggleHidden unhides a package already hidden`() {
        val (favorites, hidden) = toggleHidden(favorites = emptySet(), hidden = setOf("a"), packageName = "a")
        assertEquals(emptySet<String>(), favorites)
        assertEquals(emptySet<String>(), hidden)
    }

    @Test
    fun `toggleHidden removes favorite status when hiding a package`() {
        val (favorites, hidden) = toggleHidden(favorites = setOf("a"), hidden = emptySet(), packageName = "a")
        assertEquals(emptySet<String>(), favorites)
        assertEquals(setOf("a"), hidden)
    }
}
