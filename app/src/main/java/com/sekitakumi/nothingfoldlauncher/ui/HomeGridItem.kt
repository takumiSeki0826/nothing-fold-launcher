package com.sekitakumi.nothingfoldlauncher.ui

import com.sekitakumi.nothingfoldlauncher.data.AppFolder
import com.sekitakumi.nothingfoldlauncher.data.AppInfo

sealed interface HomeGridItem {
    data class AppItem(val app: AppInfo) : HomeGridItem
    data class FolderItem(val folder: AppFolder) : HomeGridItem
}
