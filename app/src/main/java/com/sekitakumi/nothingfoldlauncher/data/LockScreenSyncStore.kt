package com.sekitakumi.nothingfoldlauncher.data

import android.content.Context

class LockScreenSyncStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    companion object {
        private const val PREFS_NAME = "lock_screen_sync"
        private const val KEY_ENABLED = "enabled"
    }
}
