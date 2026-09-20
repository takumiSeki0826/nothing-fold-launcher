package com.sekitakumi.nothingfoldlauncher.data

fun toggleFolderSelection(current: Set<String>, packageName: String): Set<String> =
    if (packageName in current) current - packageName else current + packageName
