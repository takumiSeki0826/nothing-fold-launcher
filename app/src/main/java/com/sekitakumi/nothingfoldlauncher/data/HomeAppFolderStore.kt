package com.sekitakumi.nothingfoldlauncher.data

import android.content.Context
import java.util.UUID

class HomeAppFolderStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getFolders(): List<AppFolder> = currentOrder().mapNotNull { id ->
        val name = prefs.getString(nameKey(id), null) ?: return@mapNotNull null
        AppFolder(id, name, decodePackageList(prefs.getString(packagesKey(id), null)))
    }

    fun addFolder(name: String, packageNames: List<String>): AppFolder {
        val id = UUID.randomUUID().toString()
        prefs.edit()
            .putString(KEY_ORDER, encodePackageList(currentOrder() + id))
            .putString(nameKey(id), name)
            .putString(packagesKey(id), encodePackageList(packageNames))
            .apply()
        return AppFolder(id, name, packageNames)
    }

    fun updateFolder(id: String, name: String, packageNames: List<String>) {
        prefs.edit()
            .putString(nameKey(id), name)
            .putString(packagesKey(id), encodePackageList(packageNames))
            .apply()
    }

    fun deleteFolder(id: String) {
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
        private const val PREFS_NAME = "home_app_folders"
        private const val KEY_ORDER = "folder_order"
    }
}
