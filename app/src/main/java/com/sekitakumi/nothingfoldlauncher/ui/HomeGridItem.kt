package com.sekitakumi.nothingfoldlauncher.ui

import com.sekitakumi.nothingfoldlauncher.data.AppFolder
import com.sekitakumi.nothingfoldlauncher.data.AppInfo
import com.sekitakumi.nothingfoldlauncher.data.appRef
import com.sekitakumi.nothingfoldlauncher.data.folderRef

sealed interface HomeGridItem {
    data class AppItem(val app: AppInfo) : HomeGridItem
    data class FolderItem(val folder: AppFolder) : HomeGridItem
}

fun refOf(item: HomeGridItem): String = when (item) {
    is HomeGridItem.AppItem -> appRef(item.app.packageName)
    is HomeGridItem.FolderItem -> folderRef(item.folder.id)
}
