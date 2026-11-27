package com.sekitakumi.nothingfoldlauncher.data

import android.content.Context
import android.content.SharedPreferences
import com.sekitakumi.nothingfoldlauncher.ui.HomeKnobSlot
import com.sekitakumi.nothingfoldlauncher.ui.KnobLogo

class HomeKnobAssignmentStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    fun getTapPackage(slot: HomeKnobSlot): String? = prefs.getString(tapKey(slot), null)

    fun getFolderPackages(slot: HomeKnobSlot): List<String> = decodePackageList(prefs.getString(longPressKey(slot), null))

    fun setTapPackage(slot: HomeKnobSlot, packageName: String) {
        prefs.edit().putString(tapKey(slot), packageName).apply()
    }

    fun setFolderPackages(slot: HomeKnobSlot, packageNames: List<String>) {
        prefs.edit().putString(longPressKey(slot), encodePackageList(packageNames)).apply()
    }

    fun getName(slot: HomeKnobSlot): String? = prefs.getString(nameKey(slot), null)

    fun setName(slot: HomeKnobSlot, name: String) {
        prefs.edit().putString(nameKey(slot), name).apply()
    }

    /**
     * Runs each versioned migration step the device has not seen yet, then records the
     * version, so every step happens exactly once and later user changes are never
     * overwritten. Returns true when anything ran.
     *  - v3: HOME_1 = YouTube; a retired default (X, Claude) left in any other slot is cleared.
     *  - v4: HOME_2 = Kindle, but only if it is empty or still a retired default.
     */
    fun migrateToDefaultsIfNeeded(): Boolean {
        val from = prefs.getInt(KEY_MIGRATION_VERSION, 0)
        if (from >= MIGRATION_VERSION) return false
        val editor = prefs.edit()
        if (from < 3) {
            assign(editor, HomeKnobSlot.HOME_1, KnobLogo.YOUTUBE)
            clearRetiredDefaults(editor, except = HomeKnobSlot.HOME_1)
        }
        if (from < 4) {
            val tap = getTapPackage(HomeKnobSlot.HOME_2)
            if (tap == null || tap in RETIRED_DEFAULT_PACKAGES) assign(editor, HomeKnobSlot.HOME_2, KnobLogo.KINDLE)
        }
        editor.putInt(KEY_MIGRATION_VERSION, MIGRATION_VERSION).apply()
        return true
    }

    private fun assign(editor: SharedPreferences.Editor, slot: HomeKnobSlot, logo: KnobLogo) {
        editor.putString(tapKey(slot), logo.packageName)
        editor.putString(nameKey(slot), logo.displayName)
    }

    private fun clearRetiredDefaults(editor: SharedPreferences.Editor, except: HomeKnobSlot) {
        HomeKnobSlot.values().filter { it != except && getTapPackage(it) in RETIRED_DEFAULT_PACKAGES }.forEach { slot ->
            editor.remove(tapKey(slot))
            editor.remove(nameKey(slot))
        }
    }

    private fun tapKey(slot: HomeKnobSlot) = "${slot.id}_tap"

    private fun longPressKey(slot: HomeKnobSlot) = "${slot.id}_long"

    private fun nameKey(slot: HomeKnobSlot) = "${slot.id}_name"

    companion object {
        private const val PREFS_NAME = "home_knob_assignments"
        private const val KEY_MIGRATION_VERSION = "migration_version"
        private const val MIGRATION_VERSION = 4

        private val RETIRED_DEFAULT_PACKAGES = setOf("com.twitter.android", "com.anthropic.claude")
    }
}
