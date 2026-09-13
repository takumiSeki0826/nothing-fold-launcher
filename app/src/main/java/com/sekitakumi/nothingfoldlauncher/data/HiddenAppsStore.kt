package com.sekitakumi.nothingfoldlauncher.data

import android.content.Context

class HiddenAppsStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(): Set<String> = prefs.getStringSet(KEY_HIDDEN, null) ?: emptySet()

    fun set(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_HIDDEN, packages).apply()
    }

    companion object {
        private const val PREFS_NAME = "hidden_apps"
        private const val KEY_HIDDEN = "hidden_packages"
    }
}
