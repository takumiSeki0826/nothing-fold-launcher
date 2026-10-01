package com.sekitakumi.nothingfoldlauncher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class GroupedAppsTest {

    private val camera = AppInfo(label = "Camera", packageName = "com.example.camera")
    private val calendar = AppInfo(label = "Calendar", packageName = "com.example.calendar")
    private val settings = AppInfo(label = "Settings", packageName = "com.example.settings")
    private val apps = listOf(camera, calendar, settings)

    @Test
    fun `no groups returns all apps`() {
        assertEquals(apps, excludeGroupedApps(apps, emptyList(), ""))
    }

    @Test
    fun `removes apps that belong to a group`() {
        val groups = listOf(DrawerAppGroup("1", "G", listOf(camera.packageName)))
        assertEquals(listOf(calendar, settings), excludeGroupedApps(apps, groups, ""))
    }

    @Test
    fun `app in multiple groups is removed once and others are kept`() {
        val groups = listOf(
            DrawerAppGroup("1", "A", listOf(camera.packageName, calendar.packageName)),
            DrawerAppGroup("2", "B", listOf(camera.packageName)),
        )
        assertEquals(listOf(settings), excludeGroupedApps(apps, groups, ""))
    }

    @Test
    fun `returns empty when every app is grouped`() {
        val groups = listOf(DrawerAppGroup("1", "All", apps.map { it.packageName }))
        assertEquals(emptyList<AppInfo>(), excludeGroupedApps(apps, groups, ""))
    }

    @Test
    fun `ignores grouped packages that are not installed`() {
        val groups = listOf(DrawerAppGroup("1", "G", listOf("com.example.missing")))
        assertEquals(apps, excludeGroupedApps(apps, groups, ""))
    }

    @Test
    fun `keeps grouped apps while searching`() {
        val groups = listOf(DrawerAppGroup("1", "G", listOf(camera.packageName)))
        assertEquals(apps, excludeGroupedApps(apps, groups, "cam"))
    }

    @Test
    fun `blank query is treated as not searching`() {
        val groups = listOf(DrawerAppGroup("1", "G", listOf(camera.packageName)))
        assertEquals(listOf(calendar, settings), excludeGroupedApps(apps, groups, "  "))
    }

    @Test
    fun `home folder apps are not excluded when no drawer group exists`() {
        val homeFolder = AppFolder("h", "Home", listOf(camera.packageName))
        val drawerGroups = emptyList<DrawerAppGroup>()
        assertEquals(apps, excludeGroupedApps(apps, drawerGroups, ""))
        assertEquals(listOf(camera.packageName), homeFolder.packageNames)
    }

    @Test
    fun `only drawer group apps are excluded`() {
        val drawerGroups = listOf(DrawerAppGroup("d", "Drawer", listOf(calendar.packageName)))
        assertEquals(listOf(camera, settings), excludeGroupedApps(apps, drawerGroups, ""))
    }
}
