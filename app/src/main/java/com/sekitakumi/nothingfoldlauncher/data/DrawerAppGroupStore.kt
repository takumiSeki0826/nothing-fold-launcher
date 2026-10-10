package com.sekitakumi.nothingfoldlauncher.data

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

class DrawerAppGroupStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    fun getGroups(): List<DrawerAppGroup> = currentOrder().mapNotNull { id ->
        val name = prefs.getString(nameKey(id), null) ?: return@mapNotNull null
        DrawerAppGroup(id, name, decodePackageList(prefs.getString(packagesKey(id), null)))
    }

    fun addGroup(name: String, packageNames: List<String>): DrawerAppGroup {
        val id = UUID.randomUUID().toString()
        prefs.edit()
            .putString(KEY_ORDER, encodePackageList(currentOrder() + id))
            .putString(nameKey(id), name)
            .putString(packagesKey(id), encodePackageList(packageNames))
            .apply()
        return DrawerAppGroup(id, name, packageNames)
    }

    fun updateGroup(id: String, name: String, packageNames: List<String>) {
        prefs.edit()
            .putString(nameKey(id), name)
            .putString(packagesKey(id), encodePackageList(packageNames))
            .apply()
    }

    fun swapGroups(idA: String, idB: String) {
        val newOrder = swapHomeOrder(currentOrder(), idA, idB)
        prefs.edit().putString(KEY_ORDER, encodePackageList(newOrder)).apply()
    }

    fun deleteGroup(id: String) {
        prefs.edit()
            .putString(KEY_ORDER, encodePackageList(currentOrder() - id))
            .remove(nameKey(id))
            .remove(packagesKey(id))
            .apply()
    }

    private fun currentOrder(): List<String> = decodePackageList(prefs.getString(KEY_ORDER, null))

    private fun nameKey(id: String) = "${id}_name"

    private fun packagesKey(id: String) = "${id}_packages"

    companion object {
        private const val PREFS_NAME = "drawer_app_groups"
        private const val KEY_ORDER = "group_order"
    }
}
