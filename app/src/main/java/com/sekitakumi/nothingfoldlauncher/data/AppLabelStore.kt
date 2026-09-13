package com.sekitakumi.nothingfoldlauncher.data

import android.content.Context

class AppLabelStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAll(): Map<String, String> =
        prefs.all.mapNotNull { (packageName, label) -> (label as? String)?.let { packageName to it } }.toMap()

    fun set(packageName: String, label: String) {
        prefs.edit().putString(packageName, label).apply()
    }

    fun remove(packageName: String) {
        prefs.edit().remove(packageName).apply()
    }

    companion object {
        private const val PREFS_NAME = "app_label_overrides"
    }
}
