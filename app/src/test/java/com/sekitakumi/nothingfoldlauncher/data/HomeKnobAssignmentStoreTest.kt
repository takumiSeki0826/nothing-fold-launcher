package com.sekitakumi.nothingfoldlauncher.data

import com.sekitakumi.nothingfoldlauncher.ui.HomeKnobSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeKnobAssignmentStoreTest {

    private val prefs = FakeSharedPreferences()
    private val store = HomeKnobAssignmentStore(prefs)

    @Test
    fun `migration overwrites an existing assignment with YouTube and X`() {
        store.setTapPackage(HomeKnobSlot.HOME_1, "com.sec.android.app.myfiles")
        store.setName(HomeKnobSlot.HOME_1, "ファイル")

        assertTrue(store.migrateToDefaultsIfNeeded())

        assertEquals("com.google.android.youtube", store.getTapPackage(HomeKnobSlot.HOME_1))
        assertEquals("YouTube", store.getName(HomeKnobSlot.HOME_1))
        assertEquals("com.twitter.android", store.getTapPackage(HomeKnobSlot.HOME_2))
        assertEquals("X", store.getName(HomeKnobSlot.HOME_2))
    }

    @Test
    fun `migration seeds a fresh install`() {
        assertTrue(store.migrateToDefaultsIfNeeded())
        assertEquals("com.google.android.youtube", store.getTapPackage(HomeKnobSlot.HOME_1))
        assertEquals("com.twitter.android", store.getTapPackage(HomeKnobSlot.HOME_2))
    }

    @Test
    fun `migration runs only once so later user changes survive`() {
        store.migrateToDefaultsIfNeeded()
        store.setTapPackage(HomeKnobSlot.HOME_1, "com.example.mine")
        store.setName(HomeKnobSlot.HOME_1, "Mine")

        assertFalse(store.migrateToDefaultsIfNeeded())

        assertEquals("com.example.mine", store.getTapPackage(HomeKnobSlot.HOME_1))
        assertEquals("Mine", store.getName(HomeKnobSlot.HOME_1))
    }

    @Test
    fun `migration keeps long-press folder contents`() {
        store.setFolderPackages(HomeKnobSlot.HOME_2, listOf("a", "b"))
        store.migrateToDefaultsIfNeeded()
        assertEquals(listOf("a", "b"), store.getFolderPackages(HomeKnobSlot.HOME_2))
    }
}
