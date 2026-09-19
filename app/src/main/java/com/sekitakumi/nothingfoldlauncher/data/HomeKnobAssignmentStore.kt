package com.sekitakumi.nothingfoldlauncher.data

import android.content.Context
import com.sekitakumi.nothingfoldlauncher.ui.HomeKnobSlot

class HomeKnobAssignmentStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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

    private fun tapKey(slot: HomeKnobSlot) = "${slot.id}_tap"

    private fun longPressKey(slot: HomeKnobSlot) = "${slot.id}_long"

    private fun nameKey(slot: HomeKnobSlot) = "${slot.id}_name"

    companion object {
        private const val PREFS_NAME = "home_knob_assignments"
    }
}
