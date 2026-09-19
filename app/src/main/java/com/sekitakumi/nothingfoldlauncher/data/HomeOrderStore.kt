package com.sekitakumi.nothingfoldlauncher.data

import android.content.Context

class HomeOrderStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getOrder(): List<String> = decodePackageList(prefs.getString(KEY_ORDER, null))

    fun setOrder(order: List<String>) {
        prefs.edit().putString(KEY_ORDER, encodePackageList(order)).apply()
    }

    companion object {
        private const val PREFS_NAME = "home_order"
        private const val KEY_ORDER = "order"
    }
}
