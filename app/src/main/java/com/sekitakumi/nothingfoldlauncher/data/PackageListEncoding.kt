package com.sekitakumi.nothingfoldlauncher.data

fun encodePackageList(packages: List<String>): String = packages.joinToString(",")

fun decodePackageList(raw: String?): List<String> =
    raw?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
