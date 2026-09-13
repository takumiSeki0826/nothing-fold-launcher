package com.sekitakumi.nothingfoldlauncher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class FavoritesTest {

    private val apps = listOf(
        AppInfo(label = "Camera", packageName = "com.example.camera"),
        AppInfo(label = "Calendar", packageName = "com.example.calendar"),
        AppInfo(label = "Settings", packageName = "com.example.settings"),
    )

    @Test
    fun `defaultFavorites takes the first N package names in order`() {
        assertEquals(
            listOf("com.example.camera", "com.example.calendar"),
            defaultFavorites(apps, limit = 2),
        )
    }

    @Test
    fun `defaultFavorites returns all package names when limit exceeds size`() {
        assertEquals(
            apps.map { it.packageName },
            defaultFavorites(apps, limit = 10),
        )
    }

    @Test
    fun `homeAppsFrom keeps only apps whose package is in favorites, preserving app list order`() {
        val favorites = setOf("com.example.settings", "com.example.camera")
        assertEquals(
            listOf(apps[0], apps[2]),
            homeAppsFrom(apps, favorites),
        )
    }

    @Test
    fun `homeAppsFrom returns empty list when no favorites match`() {
        assertEquals(emptyList<AppInfo>(), homeAppsFrom(apps, emptySet()))
    }
}
