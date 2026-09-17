package com.sekitakumi.nothingfoldlauncher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sekitakumi.nothingfoldlauncher.data.AppInfo
import com.sekitakumi.nothingfoldlauncher.data.AppLabelStore
import com.sekitakumi.nothingfoldlauncher.data.AppRepository
import com.sekitakumi.nothingfoldlauncher.data.FavoritesStore
import com.sekitakumi.nothingfoldlauncher.data.HiddenAppsStore
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
) : ViewModel() {

    private val allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private val _favorites = MutableStateFlow(favoritesStore.get())
    private val _hiddenApps = MutableStateFlow(hiddenAppsStore.get())
    private val _labelOverrides = MutableStateFlow(appLabelStore.getAll())

    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()
    val hiddenApps: StateFlow<Set<String>> = _hiddenApps.asStateFlow()

    private val displayApps: StateFlow<List<AppInfo>> =
        combine(allApps, _labelOverrides, ::applyLabelOverrides)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val visibleApps: StateFlow<List<AppInfo>> =
        combine(displayApps, _query, _hiddenApps) { apps, query, hidden -> visibleAppsFor(apps, query, hidden) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val homeApps: StateFlow<List<AppInfo>> =
        combine(displayApps, _favorites) { all, favorites ->
            val effectiveFavorites = favorites.ifEmpty { defaultFavorites(all, DEFAULT_HOME_APP_COUNT).toSet() }
            homeAppsFrom(all, effectiveFavorites)
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

    private fun effectiveFavorites(): Set<String> =
        _favorites.value.ifEmpty { defaultFavorites(allApps.value, DEFAULT_HOME_APP_COUNT).toSet() }

    private fun applyVisibilityChange(newFavorites: Set<String>, newHidden: Set<String>) {
        _favorites.value = newFavorites
        _hiddenApps.value = newHidden
        favoritesStore.set(newFavorites)
        hiddenAppsStore.set(newHidden)
    }
}
