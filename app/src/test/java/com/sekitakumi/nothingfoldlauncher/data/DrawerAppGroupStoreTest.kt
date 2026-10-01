package com.sekitakumi.nothingfoldlauncher.data

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawerAppGroupStoreTest {

    private val prefs = FakeSharedPreferences()
    private val store = DrawerAppGroupStore(prefs)

    @Test
    fun `empty store returns no groups`() {
        assertEquals(emptyList<DrawerAppGroup>(), store.getGroups())
    }

    @Test
    fun `add then get round trips`() {
        val added = store.addGroup("Work", listOf("a", "b"))
        assertEquals(listOf(DrawerAppGroup(added.id, "Work", listOf("a", "b"))), store.getGroups())
    }

    @Test
    fun `groups keep insertion order`() {
        val first = store.addGroup("One", listOf("a"))
        val second = store.addGroup("Two", listOf("b"))
        assertEquals(listOf(first.id, second.id), store.getGroups().map { it.id })
        assertNotEquals(first.id, second.id)
    }

    @Test
    fun `update changes name and packages and keeps order`() {
        val first = store.addGroup("One", listOf("a"))
        val second = store.addGroup("Two", listOf("b"))
        store.updateGroup(first.id, "Uno", listOf("c", "d"))
        assertEquals(
            listOf(
                DrawerAppGroup(first.id, "Uno", listOf("c", "d")),
                DrawerAppGroup(second.id, "Two", listOf("b")),
            ),
            store.getGroups(),
        )
    }

    @Test
    fun `delete removes group and its keys`() {
        val first = store.addGroup("One", listOf("a"))
        val second = store.addGroup("Two", listOf("b"))
        store.deleteGroup(first.id)
        assertEquals(listOf(second.id), store.getGroups().map { it.id })
        assertTrue(prefs.all.keys.none { it.startsWith(first.id) })
    }

    @Test
    fun `delete of unknown id leaves other groups intact`() {
        val first = store.addGroup("One", listOf("a"))
        store.deleteGroup("missing")
        assertEquals(listOf(first), store.getGroups())
    }

    @Test
    fun `update of unknown id does not create a visible group`() {
        val first = store.addGroup("One", listOf("a"))
        store.updateGroup("missing", "X", listOf("z"))
        assertEquals(listOf(first), store.getGroups())
    }

    @Test
    fun `uses keys distinct from home folder store`() {
        store.addGroup("One", listOf("a"))
        assertTrue("folder_order" !in prefs.all.keys)
        assertTrue("group_order" in prefs.all.keys)
    }
}

/** メモリ上の最小 SharedPreferences。文字列の読み書きのみ対応。 */
private class FakeSharedPreferences : SharedPreferences {
    private val data = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = data.toMutableMap()
    override fun getString(key: String?, defValue: String?): String? = data[key] as String? ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues
    override fun getInt(key: String?, defValue: Int): Int = defValue
    override fun getLong(key: String?, defValue: Long): Long = defValue
    override fun getFloat(key: String?, defValue: Float): Float = defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = defValue
    override fun contains(key: String?): Boolean = data.containsKey(key)
    override fun edit(): SharedPreferences.Editor = Editor()
    override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
    override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

    private inner class Editor : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private val removals = mutableSetOf<String>()

        override fun putString(key: String, value: String?) = apply { pending[key] = value }
        override fun remove(key: String) = apply { removals += key }
        override fun apply() {
            removals.forEach { data.remove(it) }
            pending.forEach { (k, v) -> data[k] = v }
        }
        override fun commit(): Boolean { apply(); return true }
        override fun clear() = apply { data.clear() }
        override fun putStringSet(key: String, values: MutableSet<String>?) = this
        override fun putInt(key: String, value: Int) = this
        override fun putLong(key: String, value: Long) = this
        override fun putFloat(key: String, value: Float) = this
        override fun putBoolean(key: String, value: Boolean) = this
    }
}
