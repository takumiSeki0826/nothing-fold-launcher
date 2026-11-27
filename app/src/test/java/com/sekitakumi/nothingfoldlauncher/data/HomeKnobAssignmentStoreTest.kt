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

    private fun versionedStore(version: Int): HomeKnobAssignmentStore {
        prefs.edit().putInt("migration_version", version).apply()
        return store
    }

    @Test
    fun `fresh install gets YouTube on HOME_1 and Kindle on HOME_2`() {
        assertTrue(store.migrateToDefaultsIfNeeded())
        assertEquals("com.google.android.youtube", store.getTapPackage(HomeKnobSlot.HOME_1))
        assertEquals("YouTube", store.getName(HomeKnobSlot.HOME_1))
        assertEquals("com.amazon.kindle", store.getTapPackage(HomeKnobSlot.HOME_2))
        assertEquals("Kindle", store.getName(HomeKnobSlot.HOME_2))
    }

    @Test
    fun `old HOME_1 assignment is replaced by YouTube`() {
        store.setTapPackage(HomeKnobSlot.HOME_1, "com.sec.android.app.myfiles")
        store.setName(HomeKnobSlot.HOME_1, "ファイル")
        store.migrateToDefaultsIfNeeded()
        assertEquals("com.google.android.youtube", store.getTapPackage(HomeKnobSlot.HOME_1))
        assertEquals("YouTube", store.getName(HomeKnobSlot.HOME_1))
    }

    @Test
    fun `v3 device with an empty HOME_2 gets Kindle and keeps HOME_1`() {
        val s = versionedStore(3)
        s.setTapPackage(HomeKnobSlot.HOME_1, "com.example.mine")

        assertTrue(s.migrateToDefaultsIfNeeded())

        assertEquals("com.amazon.kindle", s.getTapPackage(HomeKnobSlot.HOME_2))
        assertEquals("com.example.mine", s.getTapPackage(HomeKnobSlot.HOME_1))
    }

    @Test
    fun `retired X and Claude defaults on HOME_2 become Kindle`() {
        for (retired in listOf("com.twitter.android", "com.anthropic.claude")) {
            val p = FakeSharedPreferences()
            val s = HomeKnobAssignmentStore(p)
            p.edit().putInt("migration_version", 2).apply()
            s.setTapPackage(HomeKnobSlot.HOME_2, retired)
            s.setName(HomeKnobSlot.HOME_2, "old")

            assertTrue(s.migrateToDefaultsIfNeeded())

            assertEquals("com.amazon.kindle", s.getTapPackage(HomeKnobSlot.HOME_2))
            assertEquals("Kindle", s.getName(HomeKnobSlot.HOME_2))
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
        prefs.edit().remove("home_2_tap").remove("home_2_name").apply()

        assertFalse(store.migrateToDefaultsIfNeeded())

        assertEquals("com.example.mine", store.getTapPackage(HomeKnobSlot.HOME_1))
        assertEquals("Mine", store.getName(HomeKnobSlot.HOME_1))
        assertNull(store.getTapPackage(HomeKnobSlot.HOME_2))
    }

    @Test
    fun `migration keeps long-press folder contents`() {
        store.setFolderPackages(HomeKnobSlot.HOME_2, listOf("a", "b"))
        store.migrateToDefaultsIfNeeded()
        assertEquals(listOf("a", "b"), store.getFolderPackages(HomeKnobSlot.HOME_2))
    }
}
