package com.sekitakumi.nothingfoldlauncher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sekitakumi.nothingfoldlauncher.data.AppInfo
import com.sekitakumi.nothingfoldlauncher.data.AppRepository
import com.sekitakumi.nothingfoldlauncher.data.FavoritesStore
import com.sekitakumi.nothingfoldlauncher.data.defaultFavorites
import com.sekitakumi.nothingfoldlauncher.data.filterApps
import com.sekitakumi.nothingfoldlauncher.data.homeAppsFrom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val DEFAULT_HOME_APP_COUNT = 8

class AppListViewModel(
    private val repository: AppRepository,
    private val favoritesStore: FavoritesStore,
) : ViewModel() {

    private val allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private val _favorites = MutableStateFlow(favoritesStore.get())

    val visibleApps: StateFlow<List<AppInfo>> =
        combine(allApps, _query, ::filterApps)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    val homeApps: StateFlow<List<AppInfo>> =
        combine(allApps, _favorites) { all, favorites ->
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
                _errorMessage.value = "アプリ一覧の取得に失敗しました"
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun toggleFavorite(packageName: String) {
        val current = _favorites.value.ifEmpty { defaultFavorites(allApps.value, DEFAULT_HOME_APP_COUNT).toSet() }
        val updated = if (packageName in current) current - packageName else current + packageName
        _favorites.value = updated
        favoritesStore.set(updated)
    }
}
