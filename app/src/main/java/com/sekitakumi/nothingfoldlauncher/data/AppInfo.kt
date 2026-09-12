package com.sekitakumi.nothingfoldlauncher.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: Drawable? = null,
)
