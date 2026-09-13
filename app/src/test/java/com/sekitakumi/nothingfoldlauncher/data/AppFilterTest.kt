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

    @Test
    fun `visibleAppsFor excludes hidden apps when query is blank`() {
        val hidden = setOf("com.example.calendar")
        val result = visibleAppsFor(apps, query = "", hidden = hidden)
        assertEquals(listOf(apps[0], apps[2]), result)
    }

    @Test
    fun `visibleAppsFor includes hidden apps when query matches them`() {
        val hidden = setOf("com.example.calendar")
        val result = visibleAppsFor(apps, query = "calendar", hidden = hidden)
        assertEquals(listOf(apps[1]), result)
    }

    @Test
    fun `visibleAppsFor with blank query and no hidden apps returns everything`() {
        val result = visibleAppsFor(apps, query = "", hidden = emptySet())
        assertEquals(apps, result)
    }
}
