package com.sekitakumi.nothingfoldlauncher.data

fun applyLabelOverrides(apps: List<AppInfo>, overrides: Map<String, String>): List<AppInfo> =
    apps.map { app -> overrides[app.packageName]?.let { app.copy(label = it) } ?: app }
        .sortedBy { it.label.lowercase() }
