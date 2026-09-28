package com.sekitakumi.nothingfoldlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.sekitakumi.nothingfoldlauncher.data.AppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(
    apps: List<AppInfo>,
    query: String,
    favorites: Set<String>,
    onQueryChange: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onSwipeDownToClose: () -> Unit,
    isExpandedWidth: Boolean = false,
    selectedPackages: Set<String>? = null,
    onToggleSelected: (AppInfo) -> Unit = {},
    onConfirmSelection: () -> Unit = {},
    systemStats: SystemStatsState = SystemStatsState(),
    onSystemStatsNetClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val letterIndexMap = remember(apps) { letterIndexMap(apps) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val closeThresholdPx = with(LocalDensity.current) { 96.dp.toPx() }
    val closeGesture = remember(closeThresholdPx) { DrawerCloseGesture(closeThresholdPx) }
    // Lifting the finger ends the gesture, so a half-finished swipe must not
    // carry over into the next one.
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) closeGesture.onGestureEnd()
    }
    val nestedScrollConnection = remember(listState, closeGesture) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput) return Offset.Zero
                val atTop = listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0
                if (closeGesture.onOverscroll(availableY = available.y, atTop = atTop)) {
                    onSwipeDownToClose()
                }
                return Offset.Zero
            }
        }
    }

    if (isExpandedWidth) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            AlphabetJogWheel(
                onLetterSelected = { letter ->
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    scrollIndexForLetter(letter, letterIndexMap, apps.size)?.let { target ->
                        coroutineScope.launch { listState.scrollToItem(target) }
                    }
                },
                onClearSearch = { onQueryChange("") },
                onJumpToStart = { coroutineScope.launch { listState.scrollToItem(0) } },
                onJumpToEnd = {
                    coroutineScope.launch { listState.scrollToItem((apps.size - 1).coerceAtLeast(0)) }
                },
                systemStats = systemStats,
                onSystemStatsNetClick = onSystemStatsNetClick,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )

            SearchColumn(
                apps = apps,
                query = query,
                favorites = favorites,
                onQueryChange = onQueryChange,
                onAppClick = onAppClick,
                onAppLongClick = onAppLongClick,
                selectedPackages = selectedPackages,
                onToggleSelected = onToggleSelected,
                onConfirmSelection = onConfirmSelection,
                listState = listState,
                coroutineScope = coroutineScope,
                letterIndexMap = letterIndexMap,
                keyboardController = keyboardController,
                focusManager = focusManager,
                nestedScrollConnection = nestedScrollConnection,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    } else {
        SearchColumn(
            apps = apps,
            query = query,
            favorites = favorites,
            onQueryChange = onQueryChange,
            onAppClick = onAppClick,
            onAppLongClick = onAppLongClick,
            selectedPackages = selectedPackages,
            onToggleSelected = onToggleSelected,
            onConfirmSelection = onConfirmSelection,
            listState = listState,
            coroutineScope = coroutineScope,
            letterIndexMap = letterIndexMap,
            keyboardController = keyboardController,
            focusManager = focusManager,
            nestedScrollConnection = nestedScrollConnection,
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 48.dp),
        )
    }
}

@Composable
private fun SearchColumn(
    apps: List<AppInfo>,
    query: String,
    favorites: Set<String>,
    onQueryChange: (String) -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    selectedPackages: Set<String>?,
    onToggleSelected: (AppInfo) -> Unit,
    onConfirmSelection: () -> Unit,
    listState: LazyListState,
    coroutineScope: CoroutineScope,
    letterIndexMap: Map<Char, Int>,
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager,
    nestedScrollConnection: NestedScrollConnection,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search", color = Color.Gray) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Black,
                unfocusedContainerColor = Color.Black,
            ),
        )

        if (selectedPackages != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "${selectedPackages.size}個選択中", color = Color.Gray)
                Text(
                    text = "完了",
                    color = Color(0xFFD1432B),
                    modifier = Modifier.clickable(onClick = onConfirmSelection),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 16.dp)
                .nestedScroll(nestedScrollConnection),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                if (shouldShowEmptyState(apps, query)) {
                    // Kept inside the list so the swipe-down-to-close gesture
                    // still has a scrollable to overscroll against.
                    item {
                        Text(
                            text = "\"$query\" に一致するアプリはありません",
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        )
                    }
                }
                items(apps, key = { it.packageName }) { app ->
                    AppRow(
                        app = app,
                        isFavorite = app.packageName in favorites,
                        isSelected = selectedPackages?.contains(app.packageName),
                        onClick = if (selectedPackages != null) {
                            { onToggleSelected(app) }
                        } else {
                            { onAppClick(app) }
                        },
                        onLongClick = if (selectedPackages != null) {
                            { onToggleSelected(app) }
                        } else {
                            { onAppLongClick(app) }
                        },
                    )
                }
            }

            if (shouldShowAlphabetIndex(apps)) {
                Spacer(modifier = Modifier.width(8.dp))
                AlphabetIndexBar(
                    onLetterSelected = { letter ->
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        scrollIndexForLetter(letter, letterIndexMap, apps.size)?.let { target ->
                            coroutineScope.launch { listState.scrollToItem(target) }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun AppRow(
    app: AppInfo,
    isFavorite: Boolean,
    isSelected: Boolean?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = app.label,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (isSelected != null) {
            Text(
                text = if (isSelected) "☑" else "☐",
                color = if (isSelected) Color(0xFFD1432B) else Color.Gray,
                modifier = Modifier.padding(end = 8.dp),
            )
        } else if (isFavorite) {
            Text(text = "★", color = Color(0xFFD1432B), modifier = Modifier.padding(end = 8.dp))
        }
    }
}
