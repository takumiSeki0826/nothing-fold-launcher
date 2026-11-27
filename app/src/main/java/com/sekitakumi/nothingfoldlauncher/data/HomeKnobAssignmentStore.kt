package com.sekitakumi.nothingfoldlauncher.data

import android.content.Context
import android.content.SharedPreferences
import com.sekitakumi.nothingfoldlauncher.ui.HomeKnobSlot

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
     * One-time, versioned reset of the knob tap targets and names to [DEFAULT_ASSIGNMENTS]
     * (HOME_1 = YouTube, HOME_2 = X). Long-press folder contents are left alone. Returns
     * true when it ran; afterwards the user's own choices are never overwritten again.
     */
    fun migrateToDefaultsIfNeeded(): Boolean {
        if (prefs.getInt(KEY_MIGRATION_VERSION, 0) >= MIGRATION_VERSION) return false
        val editor = prefs.edit()
        DEFAULT_ASSIGNMENTS.forEach { (slot, assignment) ->
            editor.putString(tapKey(slot), assignment.packageName)
            editor.putString(nameKey(slot), assignment.name)
        }
        editor.putInt(KEY_MIGRATION_VERSION, MIGRATION_VERSION).apply()
        return true
    }

    private fun tapKey(slot: HomeKnobSlot) = "${slot.id}_tap"

    private fun longPressKey(slot: HomeKnobSlot) = "${slot.id}_long"

    private fun nameKey(slot: HomeKnobSlot) = "${slot.id}_name"

    data class DefaultAssignment(val packageName: String, val name: String)

    companion object {
        private const val PREFS_NAME = "home_knob_assignments"
        private const val KEY_MIGRATION_VERSION = "migration_version"
        private const val MIGRATION_VERSION = 1

        val DEFAULT_ASSIGNMENTS: Map<HomeKnobSlot, DefaultAssignment> = mapOf(
            HomeKnobSlot.HOME_1 to DefaultAssignment("com.google.android.youtube", "YouTube"),
            HomeKnobSlot.HOME_2 to DefaultAssignment("com.twitter.android", "X"),
        )
    }
}
