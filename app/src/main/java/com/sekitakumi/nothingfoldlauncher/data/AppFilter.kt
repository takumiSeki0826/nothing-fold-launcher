package com.sekitakumi.nothingfoldlauncher.data

fun filterApps(apps: List<AppInfo>, query: String): List<AppInfo> {
    if (query.isBlank()) return apps
    return apps.filter { it.label.contains(query, ignoreCase = true) }
}

fun visibleAppsFor(apps: List<AppInfo>, query: String, hidden: Set<String>): List<AppInfo> {
    if (query.isBlank()) return apps.filterNot { it.packageName in hidden }
    return filterApps(apps, query)
}
