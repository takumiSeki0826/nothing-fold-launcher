package com.sekitakumi.nothingfoldlauncher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sekitakumi.nothingfoldlauncher.data.AppFolder
import com.sekitakumi.nothingfoldlauncher.data.AppInfo
import com.sekitakumi.nothingfoldlauncher.data.AppLabelStore
import com.sekitakumi.nothingfoldlauncher.data.AppRepository
import com.sekitakumi.nothingfoldlauncher.data.FavoritesStore
import com.sekitakumi.nothingfoldlauncher.data.HiddenAppsStore
import com.sekitakumi.nothingfoldlauncher.data.HomeAppFolderStore
import com.sekitakumi.nothingfoldlauncher.data.applyLabelOverrides
import com.sekitakumi.nothingfoldlauncher.data.defaultFavorites
import com.sekitakumi.nothingfoldlauncher.data.homeAppsFrom
import com.sekitakumi.nothingfoldlauncher.data.toggleFavorite as toggleFavoritePackages
import com.sekitakumi.nothingfoldlauncher.data.toggleHidden as toggleHiddenPackages
import com.sekitakumi.nothingfoldlauncher.data.visibleAppsFor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val DEFAULT_HOME_APP_COUNT = 12

class AppListViewModel(
    private val repository: AppRepository,
    private val favoritesStore: FavoritesStore,
    private val hiddenAppsStore: HiddenAppsStore,
    private val appLabelStore: AppLabelStore,
    private val homeAppFolderStore: HomeAppFolderStore,
) : ViewModel() {

    private val allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private val _favorites = MutableStateFlow(favoritesStore.get())
    private val _hiddenApps = MutableStateFlow(hiddenAppsStore.get())
    private val _labelOverrides = MutableStateFlow(appLabelStore.getAll())
    private val _folders = MutableStateFlow(homeAppFolderStore.getFolders())

    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()
    val hiddenApps: StateFlow<Set<String>> = _hiddenApps.asStateFlow()
    val folders: StateFlow<List<AppFolder>> = _folders.asStateFlow()

    private val displayApps: StateFlow<List<AppInfo>> =
        combine(allApps, _labelOverrides, ::applyLabelOverrides)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val visibleApps: StateFlow<List<AppInfo>> =
        combine(displayApps, _query, _hiddenApps) { apps, query, hidden -> visibleAppsFor(apps, query, hidden) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val homeItems: StateFlow<List<HomeGridItem>> =
        combine(displayApps, _favorites, _folders) { all, favorites, folders ->
            val apps = effectiveHomeApps(all, favorites)
            apps.map(HomeGridItem::AppItem) + folders.map(HomeGridItem::FolderItem)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                allApps.value = repository.loadInstalledApps()
                _errorMessage.value = null
            } catch (e: Exception) {
                allApps.value = emptyList()
                _errorMessage.value = "Failed to load app list"
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun toggleFavorite(packageName: String) {
        val currentFavorites = effectiveFavorites()
        val (newFavorites, newHidden) = toggleFavoritePackages(currentFavorites, _hiddenApps.value, packageName)
        applyVisibilityChange(newFavorites, newHidden)
    }

    fun toggleHidden(packageName: String) {
        val currentFavorites = effectiveFavorites()
        val (newFavorites, newHidden) = toggleHiddenPackages(currentFavorites, _hiddenApps.value, packageName)
        applyVisibilityChange(newFavorites, newHidden)
    }

    fun renameApp(packageName: String, newLabel: String) {
        val trimmed = newLabel.trim()
        val updated = _labelOverrides.value.toMutableMap()
        if (trimmed.isEmpty()) {
            updated.remove(packageName)
            appLabelStore.remove(packageName)
        } else {
            updated[packageName] = trimmed
            appLabelStore.set(packageName, trimmed)
        }
        _labelOverrides.value = updated
    }

    fun addFolder(name: String, packageNames: List<String>): AppFolder {
        val folder = homeAppFolderStore.addFolder(name, packageNames)
        _folders.value = homeAppFolderStore.getFolders()
        return folder
    }

    fun updateFolder(id: String, name: String, packageNames: List<String>) {
        homeAppFolderStore.updateFolder(id, name, packageNames)
        _folders.value = homeAppFolderStore.getFolders()
    }

    fun deleteFolder(id: String) {
        homeAppFolderStore.deleteFolder(id)
        _folders.value = homeAppFolderStore.getFolders()
    }

    private fun effectiveHomeApps(all: List<AppInfo>, favorites: Set<String>): List<AppInfo> {
        val effectiveFavorites = favorites.ifEmpty { defaultFavorites(all, DEFAULT_HOME_APP_COUNT).toSet() }
        return homeAppsFrom(all, effectiveFavorites)
    }

    private fun effectiveFavorites(): Set<String> =
        _favorites.value.ifEmpty { defaultFavorites(allApps.value, DEFAULT_HOME_APP_COUNT).toSet() }

    private fun applyVisibilityChange(newFavorites: Set<String>, newHidden: Set<String>) {
        _favorites.value = newFavorites
        _hiddenApps.value = newHidden
        favoritesStore.set(newFavorites)
        hiddenAppsStore.set(newHidden)
    }
}
