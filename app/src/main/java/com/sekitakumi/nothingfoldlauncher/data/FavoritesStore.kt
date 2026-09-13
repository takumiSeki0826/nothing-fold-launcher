package com.sekitakumi.nothingfoldlauncher.data

import android.content.Context

class FavoritesStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun get(): Set<String> = prefs.getStringSet(KEY_FAVORITES, null) ?: emptySet()

    fun set(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITES, packages).apply()
    }

    companion object {
        private const val PREFS_NAME = "favorites"
        private const val KEY_FAVORITES = "favorite_packages"
    }
}
