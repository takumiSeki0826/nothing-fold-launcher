package com.sekitakumi.nothingfoldlauncher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class FolderSelectionTest {

    @Test
    fun `adding a package appends it to the end`() {
        val current = linkedSetOf("a", "b")
        assertEquals(listOf("a", "b", "c"), toggleFolderSelection(current, "c").toList())
    }

    @Test
    fun `removing a package preserves the order of the remaining ones`() {
        val current = linkedSetOf("a", "b", "c")
        assertEquals(listOf("a", "c"), toggleFolderSelection(current, "b").toList())
    }

    @Test
    fun `re-adding a previously removed package appends it at the end, not its old position`() {
        val current = linkedSetOf("a", "b", "c")
        val afterRemove = toggleFolderSelection(current, "b")
        val afterReadd = toggleFolderSelection(afterRemove, "b")
        assertEquals(listOf("a", "c", "b"), afterReadd.toList())
    }
}
