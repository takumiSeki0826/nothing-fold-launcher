package com.sekitakumi.nothingfoldlauncher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AppFilterTest {

    private val apps = listOf(
        AppInfo(label = "Camera", packageName = "com.example.camera"),
        AppInfo(label = "Calendar", packageName = "com.example.calendar"),
        AppInfo(label = "Settings", packageName = "com.example.settings"),
    )

    @Test
    fun `empty query returns all apps`() {
        val result = filterApps(apps, "")
        assertEquals(apps, result)
    }

    @Test
    fun `filters by case-insensitive substring match on label`() {
        val result = filterApps(apps, "cam")
        assertEquals(listOf(apps[0]), result)
    }

    @Test
    fun `matches regardless of query case`() {
        val result = filterApps(apps, "CALENDAR")
        assertEquals(listOf(apps[1]), result)
    }

    @Test
    fun `returns empty list when nothing matches`() {
        val result = filterApps(apps, "xyz")
        assertEquals(emptyList<AppInfo>(), result)
    }
}
