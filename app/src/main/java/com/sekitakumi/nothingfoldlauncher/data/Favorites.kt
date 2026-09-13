package com.sekitakumi.nothingfoldlauncher.data

fun defaultFavorites(apps: List<AppInfo>, limit: Int): List<String> =
    apps.take(limit).map { it.packageName }

fun homeAppsFrom(all: List<AppInfo>, favoritePackages: Set<String>): List<AppInfo> =
    all.filter { it.packageName in favoritePackages }
