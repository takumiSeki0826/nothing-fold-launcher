package com.sekitakumi.nothingfoldlauncher.data

import android.content.Context

class MobileDataBaselineStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getBaselineBytes(): Long? =
        if (prefs.contains(KEY_BASELINE_BYTES)) prefs.getLong(KEY_BASELINE_BYTES, 0L) else null

    fun getBaselineDate(): String? = prefs.getString(KEY_BASELINE_DATE, null)

    fun setBaseline(bytes: Long, date: String) {
        prefs.edit()
            .putLong(KEY_BASELINE_BYTES, bytes)
            .putString(KEY_BASELINE_DATE, date)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "mobile_data_baseline"
        private const val KEY_BASELINE_BYTES = "baseline_bytes"
        private const val KEY_BASELINE_DATE = "baseline_date"
    }
}
