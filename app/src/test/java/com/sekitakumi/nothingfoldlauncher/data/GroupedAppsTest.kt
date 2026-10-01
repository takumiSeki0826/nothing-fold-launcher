package com.sekitakumi.nothingfoldlauncher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class GroupedAppsTest {

    private val camera = AppInfo(label = "Camera", packageName = "com.example.camera")
    private val calendar = AppInfo(label = "Calendar", packageName = "com.example.calendar")
    private val settings = AppInfo(label = "Settings", packageName = "com.example.settings")
    private val apps = listOf(camera, calendar, settings)

    @Test
    fun `no folders returns all apps`() {
        assertEquals(apps, excludeGroupedApps(apps, emptyList(), ""))
    }

    @Test
    fun `removes apps that belong to a folder`() {
        val folders = listOf(AppFolder("1", "G", listOf(camera.packageName)))
        assertEquals(listOf(calendar, settings), excludeGroupedApps(apps, folders, ""))
    }

    @Test
    fun `app in multiple folders is removed once and others are kept`() {
        val folders = listOf(
            AppFolder("1", "A", listOf(camera.packageName, calendar.packageName)),
            AppFolder("2", "B", listOf(camera.packageName)),
        )
        assertEquals(listOf(settings), excludeGroupedApps(apps, folders, ""))
    }

    @Test
    fun `returns empty when every app is grouped`() {
        val folders = listOf(AppFolder("1", "All", apps.map { it.packageName }))
        assertEquals(emptyList<AppInfo>(), excludeGroupedApps(apps, folders, ""))
    }

    @Test
    fun `ignores grouped packages that are not installed`() {
        val folders = listOf(AppFolder("1", "G", listOf("com.example.missing")))
        assertEquals(apps, excludeGroupedApps(apps, folders, ""))
    }

    @Test
    fun `keeps grouped apps while searching`() {
        val folders = listOf(AppFolder("1", "G", listOf(camera.packageName)))
        assertEquals(apps, excludeGroupedApps(apps, folders, "cam"))
    }

    @Test
    fun `blank query is treated as not searching`() {
        val folders = listOf(AppFolder("1", "G", listOf(camera.packageName)))
        assertEquals(listOf(calendar, settings), excludeGroupedApps(apps, folders, "  "))
    }
}
