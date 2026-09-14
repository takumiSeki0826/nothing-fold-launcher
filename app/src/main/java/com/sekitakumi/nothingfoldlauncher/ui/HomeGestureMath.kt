package com.sekitakumi.nothingfoldlauncher.ui

import android.content.Intent

fun shouldCloseDrawerOnNewIntent(intentAction: String?, isDrawerOpen: Boolean): Boolean {
    return isDrawerOpen && intentAction == Intent.ACTION_MAIN
}
