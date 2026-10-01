package com.sekitakumi.nothingfoldlauncher.data

fun groupedPackageNames(groups: List<DrawerAppGroup>): Set<String> =
    groups.flatMapTo(mutableSetOf()) { it.packageNames }

/** 検索中はグループ内アプリも検索で見つかるよう、除外しない。 */
fun excludeGroupedApps(apps: List<AppInfo>, groups: List<DrawerAppGroup>, query: String): List<AppInfo> {
    if (query.isNotBlank()) return apps
    val grouped = groupedPackageNames(groups)
    return apps.filterNot { it.packageName in grouped }
}
