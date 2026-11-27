package com.sekitakumi.nothingfoldlauncher.data

import com.sekitakumi.nothingfoldlauncher.ui.HomeKnobSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeKnobAssignmentStoreTest {

    private val prefs = FakeSharedPreferences()
    private val store = HomeKnobAssignmentStore(prefs)

    @Test
    fun `migration sets HOME_1 to YouTube`() {
        store.setTapPackage(HomeKnobSlot.HOME_1, "com.sec.android.app.myfiles")
        store.setName(HomeKnobSlot.HOME_1, "ファイル")

        assertTrue(store.migrateToDefaultsIfNeeded())

        assertEquals("com.google.android.youtube", store.getTapPackage(HomeKnobSlot.HOME_1))
        assertEquals("YouTube", store.getName(HomeKnobSlot.HOME_1))
    }

    @Test
    fun `migration seeds a fresh install with HOME_2 left empty`() {
        assertTrue(store.migrateToDefaultsIfNeeded())
        assertEquals("com.google.android.youtube", store.getTapPackage(HomeKnobSlot.HOME_1))
        assertNull(store.getTapPackage(HomeKnobSlot.HOME_2))
        assertNull(store.getName(HomeKnobSlot.HOME_2))
    }

    @Test
    fun `retired X and Claude defaults on HOME_2 are cleared`() {
        for (retired in listOf("com.twitter.android", "com.anthropic.claude")) {
            val p = FakeSharedPreferences()
            val s = HomeKnobAssignmentStore(p)
            p.edit().putInt("migration_version", 2).apply()
            s.setTapPackage(HomeKnobSlot.HOME_2, retired)
            s.setName(HomeKnobSlot.HOME_2, "old")

            assertTrue(s.migrateToDefaultsIfNeeded())

            assertNull(s.getTapPackage(HomeKnobSlot.HOME_2))
            assertNull(s.getName(HomeKnobSlot.HOME_2))
        }
    }

    @Test
    fun `a user's own HOME_2 choice is not touched`() {
        store.setTapPackage(HomeKnobSlot.HOME_2, "com.android.chrome")
        store.setName(HomeKnobSlot.HOME_2, "Chrome")

        store.migrateToDefaultsIfNeeded()

        assertEquals("com.android.chrome", store.getTapPackage(HomeKnobSlot.HOME_2))
        assertEquals("Chrome", store.getName(HomeKnobSlot.HOME_2))
    }

    @Test
    fun `migration runs only once so later user changes survive`() {
        store.migrateToDefaultsIfNeeded()
        store.setTapPackage(HomeKnobSlot.HOME_1, "com.example.mine")
        store.setName(HomeKnobSlot.HOME_1, "Mine")
        store.setTapPackage(HomeKnobSlot.HOME_2, "com.anthropic.claude")

        assertFalse(store.migrateToDefaultsIfNeeded())

        assertEquals("com.example.mine", store.getTapPackage(HomeKnobSlot.HOME_1))
        assertEquals("Mine", store.getName(HomeKnobSlot.HOME_1))
        assertEquals("com.anthropic.claude", store.getTapPackage(HomeKnobSlot.HOME_2))
    }

    @Test
    fun `migration keeps long-press folder contents`() {
        store.setFolderPackages(HomeKnobSlot.HOME_2, listOf("a", "b"))
        store.migrateToDefaultsIfNeeded()
        assertEquals(listOf("a", "b"), store.getFolderPackages(HomeKnobSlot.HOME_2))
    }
}
