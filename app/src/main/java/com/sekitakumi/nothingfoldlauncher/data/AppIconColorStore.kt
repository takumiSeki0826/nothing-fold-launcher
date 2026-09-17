package com.sekitakumi.nothingfoldlauncher.data

import android.content.Context
import com.sekitakumi.nothingfoldlauncher.ui.IconPaletteColor

class AppIconColorStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getColor(packageName: String): IconPaletteColor? {
        val name = prefs.getString(packageName, null) ?: return null
        return runCatching { IconPaletteColor.valueOf(name) }.getOrNull()
    }

    fun setColor(packageName: String, color: IconPaletteColor) {
        prefs.edit().putString(packageName, color.name).apply()
    }

    companion object {
        private const val PREFS_NAME = "app_icon_colors"
    }
}
