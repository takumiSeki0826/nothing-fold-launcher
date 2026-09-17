package com.sekitakumi.nothingfoldlauncher.data

import android.content.Context
import com.sekitakumi.nothingfoldlauncher.ui.KnobSlot

class EqKnobAssignmentStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getTapPackage(slot: KnobSlot): String? = prefs.getString(tapKey(slot), null)

    fun getLongPressPackage(slot: KnobSlot): String? = prefs.getString(longPressKey(slot), null)

    fun setTapPackage(slot: KnobSlot, packageName: String) {
        prefs.edit().putString(tapKey(slot), packageName).apply()
    }

    fun setLongPressPackage(slot: KnobSlot, packageName: String) {
        prefs.edit().putString(longPressKey(slot), packageName).apply()
    }

    fun getName(slot: KnobSlot): String? = prefs.getString(nameKey(slot), null)

    fun setName(slot: KnobSlot, name: String) {
        prefs.edit().putString(nameKey(slot), name).apply()
    }

    private fun tapKey(slot: KnobSlot) = "${slot.id}_tap"

    private fun longPressKey(slot: KnobSlot) = "${slot.id}_long"

    private fun nameKey(slot: KnobSlot) = "${slot.id}_name"

    companion object {
        private const val PREFS_NAME = "eq_knob_assignments"
    }
}
