package com.sekitakumi.nothingfoldlauncher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLabelsTest {

    // Pre-sorted by label.lowercase(), matching what AppRepository produces in production.
    private val apps = listOf(
        AppInfo(label = "Calendar", packageName = "com.example.calendar"),
        AppInfo(label = "Camera", packageName = "com.example.camera"),
    )

    @Test
    fun `applies override label for matching package`() {
        val overrides = mapOf("com.example.camera" to "Snap")
        val result = applyLabelOverrides(apps, overrides)
        assertEquals("Snap", result.first { it.packageName == "com.example.camera" }.label)
    }

    @Test
    fun `leaves apps without an override untouched`() {
        val overrides = mapOf("com.example.camera" to "Snap")
        val result = applyLabelOverrides(apps, overrides)
        assertEquals(apps[0], result.first { it.packageName == "com.example.calendar" })
    }

    @Test
    fun `returns original list unchanged when there are no overrides`() {
        val result = applyLabelOverrides(apps, emptyMap())
        assertEquals(apps, result)
    }

    @Test
    fun `re-sorts apps by their overridden label so renamed apps land in the right alphabetical spot`() {
        val apps = listOf(
            AppInfo(label = "Alpha", packageName = "a"),
            AppInfo(label = "Middle", packageName = "b"),
            AppInfo(label = "Zulu", packageName = "c"),
        )
        val overrides = mapOf("c" to "Beta")
        val result = applyLabelOverrides(apps, overrides)
        assertEquals(listOf("Alpha", "Beta", "Middle"), result.map { it.label })
    }

    @Test
    fun `renaming a non-Latin label into English moves it under the matching letter`() {
        val apps = listOf(
            AppInfo(label = "Camera", packageName = "a"),
            AppInfo(label = "Chrome", packageName = "b"),
            AppInfo(label = "設定", packageName = "c"),
        )
        val overrides = mapOf("c" to "Setting")
        val result = applyLabelOverrides(apps, overrides)
        assertEquals(listOf("Camera", "Chrome", "Setting"), result.map { it.label })
    }
}
