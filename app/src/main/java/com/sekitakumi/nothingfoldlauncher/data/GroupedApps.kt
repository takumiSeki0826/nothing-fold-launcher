package com.sekitakumi.nothingfoldlauncher.data

fun groupedPackageNames(folders: List<AppFolder>): Set<String> =
    folders.flatMapTo(mutableSetOf()) { it.packageNames }

/** 検索中はグループ内アプリも検索で見つかるよう、除外しない。 */
fun excludeGroupedApps(apps: List<AppInfo>, folders: List<AppFolder>, query: String): List<AppInfo> {
    if (query.isNotBlank()) return apps
    val grouped = groupedPackageNames(folders)
    return apps.filterNot { it.packageName in grouped }
}
