package com.sekitakumi.nothingfoldlauncher.data

/** アプリ一覧画面(ドロワー)専用のグループ。ホームの [AppFolder] とは別データ。 */
data class DrawerAppGroup(
    val id: String,
    val name: String,
    val packageNames: List<String>,
)
