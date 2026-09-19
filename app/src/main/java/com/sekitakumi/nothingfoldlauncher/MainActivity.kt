package com.sekitakumi.nothingfoldlauncher

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.sekitakumi.nothingfoldlauncher.data.AppFolder
import com.sekitakumi.nothingfoldlauncher.data.AppIconColorStore
import com.sekitakumi.nothingfoldlauncher.data.AppInfo
import com.sekitakumi.nothingfoldlauncher.data.AppLabelStore
import com.sekitakumi.nothingfoldlauncher.data.AppRepository
import com.sekitakumi.nothingfoldlauncher.data.FavoritesStore
import com.sekitakumi.nothingfoldlauncher.data.HiddenAppsStore
import com.sekitakumi.nothingfoldlauncher.data.HomeAppFolderStore
import com.sekitakumi.nothingfoldlauncher.data.HomeKnobAssignmentStore
import com.sekitakumi.nothingfoldlauncher.data.HomeOrderStore
import com.sekitakumi.nothingfoldlauncher.data.LockScreenSyncStore
import com.sekitakumi.nothingfoldlauncher.ui.AppContextMenu
import com.sekitakumi.nothingfoldlauncher.ui.AppDrawer
import com.sekitakumi.nothingfoldlauncher.ui.AppListViewModel
import com.sekitakumi.nothingfoldlauncher.ui.FolderEditDialog
import com.sekitakumi.nothingfoldlauncher.ui.FolderOverlay
import com.sekitakumi.nothingfoldlauncher.ui.HomeAppGridSettingsMenu
import com.sekitakumi.nothingfoldlauncher.ui.HomeGridItem
import com.sekitakumi.nothingfoldlauncher.ui.HomeKnobAppAssignment
import com.sekitakumi.nothingfoldlauncher.ui.HomeKnobSettingsMenu
import com.sekitakumi.nothingfoldlauncher.ui.HomeKnobSlot
import com.sekitakumi.nothingfoldlauncher.ui.HomeRoute
import com.sekitakumi.nothingfoldlauncher.ui.HomeScreen
import com.sekitakumi.nothingfoldlauncher.ui.IconColorPickerDialog
import com.sekitakumi.nothingfoldlauncher.ui.IconPaletteColor
import com.sekitakumi.nothingfoldlauncher.ui.LockScreenSyncMenu
import com.sekitakumi.nothingfoldlauncher.ui.NowPlayingController
import com.sekitakumi.nothingfoldlauncher.ui.RenameAppDialog
import com.sekitakumi.nothingfoldlauncher.ui.StatusIconsController
import com.sekitakumi.nothingfoldlauncher.ui.VolumeController
import com.sekitakumi.nothingfoldlauncher.ui.isExpandedWidth
import com.sekitakumi.nothingfoldlauncher.ui.nextHomeRoute
import com.sekitakumi.nothingfoldlauncher.ui.shouldCloseDrawerOnNewIntent
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingFoldLauncherTheme
import com.sekitakumi.nothingfoldlauncher.wallpaper.LockWallpaperGenerator
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val YOUTUBE_MUSIC_PACKAGE = "com.google.android.apps.youtube.music"

private sealed class PendingAppPick {
    data class HomeKnobTap(val slot: HomeKnobSlot) : PendingAppPick()
}

private sealed class FolderEditTarget {
    data class HomeKnob(val slot: HomeKnobSlot) : FolderEditTarget()
    data class Grid(val folderId: String) : FolderEditTarget()
}

private data class FolderOverlayState(val name: String, val apps: List<AppInfo>)

class MainActivity : ComponentActivity() {

    private val homeRouteState = mutableStateOf(HomeRoute.HOME)
    private val pendingAppPickState = mutableStateOf<PendingAppPick?>(null)
    private val folderEditTargetState = mutableStateOf<FolderEditTarget?>(null)

    private val lockScreenSyncStore by lazy { LockScreenSyncStore(applicationContext) }
    private val homeKnobAssignmentStore by lazy { HomeKnobAssignmentStore(applicationContext) }
    private val homeAppFolderStore by lazy { HomeAppFolderStore(applicationContext) }
    private val homeOrderStore by lazy { HomeOrderStore(applicationContext) }
    private val appIconColorStore by lazy { AppIconColorStore(applicationContext) }

    private val viewModel: AppListViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                @Suppress("UNCHECKED_CAST")
                return AppListViewModel(
                    AppRepository(packageManager),
                    FavoritesStore(applicationContext),
                    HiddenAppsStore(applicationContext),
                    AppLabelStore(applicationContext),
                    homeAppFolderStore,
                    homeOrderStore,
                ) as T
            }
        }
    }

    private val requestPhoneStatePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemStatusBar()
        requestPhoneStatePermission.launch(Manifest.permission.READ_PHONE_STATE)

        setContent {
            NothingFoldLauncherTheme {
                var homeRoute by homeRouteState
                var pendingAppPick by pendingAppPickState
                var folderEditTarget by folderEditTargetState
                val isExpandedWidth = isExpandedWidth(LocalConfiguration.current.screenWidthDp)
                val apps by viewModel.visibleApps.collectAsState()
                val homeItems by viewModel.homeItems.collectAsState()
                val folders by viewModel.folders.collectAsState()
                val favorites by viewModel.favorites.collectAsState()
                val hiddenApps by viewModel.hiddenApps.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()
                val query by viewModel.query.collectAsState()

                var menuTargetApp by remember { mutableStateOf<AppInfo?>(null) }
                var renameTargetApp by remember { mutableStateOf<AppInfo?>(null) }
                val openAppMenu: (AppInfo) -> Unit = { menuTargetApp = it }

                var showLockScreenSyncMenu by remember { mutableStateOf(false) }
                var lockScreenSyncEnabled by remember { mutableStateOf(lockScreenSyncStore.isEnabled()) }
                var showHomeKnobSettings by remember { mutableStateOf(false) }
                var showAppGridSettings by remember { mutableStateOf(false) }
                var colorPickerTargetApp by remember { mutableStateOf<AppInfo?>(null) }
                val iconColorAssignments = remember { mutableStateMapOf<String, IconPaletteColor>() }
                var folderEditSelection by remember { mutableStateOf<Set<String>>(emptySet()) }
                var folderOverlay by remember { mutableStateOf<FolderOverlayState?>(null) }
                var folderEditDialogTarget by remember { mutableStateOf<String?>(null) }

                val volumeController = remember { VolumeController(applicationContext) }
                DisposableEffect(volumeController) {
                    volumeController.register()
                    onDispose { volumeController.unregister() }
                }
                val volumeRatio by volumeController.ratio.collectAsState()

                val statusIconsController = remember { StatusIconsController(applicationContext) }
                DisposableEffect(statusIconsController) {
                    statusIconsController.register()
                    onDispose { statusIconsController.unregister() }
                }
                val batteryPercent by statusIconsController.batteryPercent.collectAsState()
                val isCharging by statusIconsController.isCharging.collectAsState()
                val wifiConnected by statusIconsController.wifiConnected.collectAsState()
                val signalBars by statusIconsController.signalBars.collectAsState()
                val networkType by statusIconsController.networkType.collectAsState()

                val nowPlayingController = remember { NowPlayingController(applicationContext) }
                DisposableEffect(nowPlayingController) {
                    nowPlayingController.refresh()
                    onDispose { nowPlayingController.dispose() }
                }
                val nowPlaying by nowPlayingController.nowPlaying.collectAsState()
                val nowPlayingPermissionGranted by nowPlayingController.permissionGranted.collectAsState()

                var brightnessRatio by remember { mutableFloatStateOf(initialBrightnessRatio()) }

                var dragAccumX by remember { mutableFloatStateOf(0f) }
                var dragAccumY by remember { mutableFloatStateOf(0f) }

                val closeDrawer: () -> Unit = ::closeDrawer

                val appsByPackage = remember(apps) { apps.associateBy { it.packageName } }

                val homeKnobPackageAssignments = remember {
                    mutableStateMapOf<HomeKnobSlot, Pair<String?, List<String>>>().apply {
                        HomeKnobSlot.values().forEach { slot ->
                            put(
                                slot,
                                homeKnobAssignmentStore.getTapPackage(slot) to
                                    homeKnobAssignmentStore.getFolderPackages(slot),
                            )
                        }
                    }
                }
                val homeKnobAssignments = HomeKnobSlot.values().associateWith { slot ->
                    val (tapPackage, folderPackages) = homeKnobPackageAssignments[slot] ?: (null to emptyList())
                    HomeKnobAppAssignment(
                        tapApp = tapPackage?.let(appsByPackage::get),
                        folderApps = folderPackages.mapNotNull(appsByPackage::get),
                    )
                }
                val homeKnobNames = remember {
                    mutableStateMapOf<HomeKnobSlot, String>().apply {
                        HomeKnobSlot.values().forEach { slot ->
                            homeKnobAssignmentStore.getName(slot)?.let { put(slot, it) }
                        }
                    }
                }

                val assignPendingTapApp: (AppInfo) -> Unit = { app ->
                    (pendingAppPick as? PendingAppPick.HomeKnobTap)?.let { pick ->
                        val current = homeKnobPackageAssignments[pick.slot] ?: (null to emptyList())
                        homeKnobAssignmentStore.setTapPackage(pick.slot, app.packageName)
                        homeKnobPackageAssignments[pick.slot] = app.packageName to current.second
                    }
                    pendingAppPick = null
                    homeRoute = HomeRoute.HOME
                }

                val finishFolderEdit: () -> Unit = {
                    when (val target = folderEditTarget) {
                        is FolderEditTarget.HomeKnob -> {
                            homeKnobAssignmentStore.setFolderPackages(target.slot, folderEditSelection.toList())
                            val current = homeKnobPackageAssignments[target.slot] ?: (null to emptyList())
                            homeKnobPackageAssignments[target.slot] = current.first to folderEditSelection.toList()
                        }
                        is FolderEditTarget.Grid -> {
                            val currentName = folders.firstOrNull { it.id == target.folderId }?.name ?: ""
                            viewModel.updateFolder(target.folderId, currentName, folderEditSelection.toList())
                        }
                        null -> Unit
                    }
                    folderEditTarget = null
                    homeRoute = HomeRoute.HOME
                }

                val onDrawerDismissed: () -> Unit = {
                    pendingAppPick = null
                    folderEditTarget = null
                    closeDrawer()
                }

                val startHomeKnobTapPick: (HomeKnobSlot) -> Unit = { slot ->
                    showHomeKnobSettings = false
                    pendingAppPick = PendingAppPick.HomeKnobTap(slot)
                    homeRoute = HomeRoute.DRAWER
                }

                val startHomeKnobFolderEdit: (HomeKnobSlot) -> Unit = { slot ->
                    showHomeKnobSettings = false
                    folderEditTarget = FolderEditTarget.HomeKnob(slot)
                    folderEditSelection = (homeKnobPackageAssignments[slot]?.second ?: emptyList()).toSet()
                    homeRoute = HomeRoute.DRAWER
                }

                val startGridFolderEdit: (AppFolder) -> Unit = { folder ->
                    folderEditDialogTarget = null
                    folderEditTarget = FolderEditTarget.Grid(folder.id)
                    folderEditSelection = folder.packageNames.toSet()
                    homeRoute = HomeRoute.DRAWER
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(homeRoute) {
                            // Home: swipe up opens the drawer. Drawer: swipe left or down returns home.
                            // Descendant scrollables (the app list, the alphabet
                            // index bar) consume their own drag events, so this
                            // only sees drags that they didn't claim.
                            detectDragGestures(
                                onDragStart = {
                                    dragAccumX = 0f
                                    dragAccumY = 0f
                                },
                                onDragEnd = {
                                    val nextRoute = nextHomeRoute(homeRoute, dragAccumX, dragAccumY)
                                    if (homeRoute == HomeRoute.DRAWER && nextRoute == HomeRoute.HOME) {
                                        pendingAppPick = null
                                        folderEditTarget = null
                                    }
                                    homeRoute = nextRoute
                                    if (homeRoute == HomeRoute.HOME) viewModel.onQueryChange("")
                                    dragAccumX = 0f
                                    dragAccumY = 0f
                                },
                            ) { _, dragAmount ->
                                dragAccumX += dragAmount.x
                                dragAccumY += dragAmount.y
                            }
                        },
                ) {
                    AnimatedContent(
                        targetState = homeRoute,
                        transitionSpec = {
                            when {
                                initialState == HomeRoute.HOME && targetState == HomeRoute.DRAWER ->
                                    // Home -> drawer (swipe up): home exits upward, drawer enters from below
                                    slideInVertically { it } togetherWith slideOutVertically { -it }
                                else ->
                                    // Drawer -> home: drawer exits downward, home enters from above
                                    slideInVertically { -it } togetherWith slideOutVertically { it }
                            }
                        },
                        label = "home-route-transition",
                    ) { route ->
                        when (route) {
                            HomeRoute.DRAWER -> if (folderEditTarget != null) {
                                AppDrawer(
                                    apps = apps,
                                    query = query,
                                    favorites = favorites,
                                    onQueryChange = viewModel::onQueryChange,
                                    onAppClick = {},
                                    onAppLongClick = {},
                                    selectedPackages = folderEditSelection,
                                    onToggleSelected = { app ->
                                        folderEditSelection = if (app.packageName in folderEditSelection) {
                                            folderEditSelection - app.packageName
                                        } else {
                                            folderEditSelection + app.packageName
                                        }
                                    },
                                    onConfirmSelection = finishFolderEdit,
                                    onSwipeDownToClose = onDrawerDismissed,
                                    isExpandedWidth = isExpandedWidth,
                                )
                            } else {
                                AppDrawer(
                                    apps = apps,
                                    query = query,
                                    favorites = favorites,
                                    onQueryChange = viewModel::onQueryChange,
                                    onAppClick = { app ->
                                        if (pendingAppPick != null) assignPendingTapApp(app) else launchApp(app.packageName)
                                    },
                                    onAppLongClick = { app ->
                                        if (pendingAppPick != null) assignPendingTapApp(app) else openAppMenu(app)
                                    },
                                    onSwipeDownToClose = onDrawerDismissed,
                                    isExpandedWidth = isExpandedWidth,
                                )
                            }
                            HomeRoute.HOME -> HomeScreen(
                                items = homeItems,
                                errorMessage = errorMessage,
                                volumeRatio = volumeRatio,
                                onVolumeRatioChange = volumeController::setRatio,
                                brightnessRatio = brightnessRatio,
                                onBrightnessRatioChange = { ratio ->
                                    brightnessRatio = ratio
                                    setWindowBrightness(ratio)
                                },
                                batteryPercent = batteryPercent,
                                isCharging = isCharging,
                                wifiConnected = wifiConnected,
                                signalBars = signalBars,
                                networkType = networkType,
                                nowPlaying = nowPlaying,
                                nowPlayingPermissionGranted = nowPlayingPermissionGranted,
                                onTogglePlayPause = nowPlayingController::togglePlayPause,
                                onRequestNowPlayingPermission = { openNotificationListenerSettings() },
                                onNowPlayingClick = { launchNowPlayingApp(nowPlaying?.packageName) },
                                onNowPlayingLongClick = { launchYoutubeMusic() },
                                onCalendarClick = { launchCalendarApp() },
                                onCalendarLongClick = { showLockScreenSyncMenu = true },
                                onAppClick = { launchApp(it.packageName) },
                                onAppLongClick = openAppMenu,
                                onFolderClick = { folder ->
                                    folderOverlay = FolderOverlayState(
                                        folder.name,
                                        folder.packageNames.mapNotNull(appsByPackage::get),
                                    )
                                },
                                onFolderLongClick = { folder -> folderEditDialogTarget = folder.id },
                                onAppGridSettingsLongPress = { showAppGridSettings = true },
                                isExpandedWidth = isExpandedWidth,
                                homeKnobNames = homeKnobNames,
                                onHomeKnobTap = { slot ->
                                    val tapPackage = homeKnobPackageAssignments[slot]?.first
                                    if (tapPackage != null) {
                                        launchApp(tapPackage)
                                    } else {
                                        startHomeKnobTapPick(slot)
                                    }
                                },
                                onHomeKnobLongPress = { slot ->
                                    val folderPackages = homeKnobPackageAssignments[slot]?.second.orEmpty()
                                    if (folderPackages.isNotEmpty()) {
                                        folderOverlay = FolderOverlayState(
                                            homeKnobNames[slot] ?: slot.defaultLabel,
                                            folderPackages.mapNotNull(appsByPackage::get),
                                        )
                                    } else {
                                        startHomeKnobFolderEdit(slot)
                                    }
                                },
                                onHomeKnobSettingsLongPress = { showHomeKnobSettings = true },
                                onReorder = { from, to ->
                                    if (to != null) viewModel.swapHomeItems(from, to) else viewModel.moveHomeItemToEnd(from)
                                },
                                iconColorFor = { app ->
                                    iconColorAssignments[app.packageName] ?: appIconColorStore.getColor(app.packageName)
                                },
                            )
                        }
                    }

                    menuTargetApp?.let { app ->
                        AppContextMenu(
                            app = app,
                            isFavorite = app.packageName in favorites,
                            isHidden = app.packageName in hiddenApps,
                            onToggleFavorite = {
                                viewModel.toggleFavorite(app.packageName)
                                menuTargetApp = null
                            },
                            onToggleHidden = {
                                viewModel.toggleHidden(app.packageName)
                                menuTargetApp = null
                            },
                            onRename = {
                                renameTargetApp = app
                                menuTargetApp = null
                            },
                            onChangeColor = {
                                colorPickerTargetApp = app
                                menuTargetApp = null
                            },
                            onUninstall = {
                                uninstallApp(app.packageName)
                                menuTargetApp = null
                            },
                            onDismiss = { menuTargetApp = null },
                        )
                    }

                    colorPickerTargetApp?.let { app ->
                        IconColorPickerDialog(
                            app = app,
                            selectedColor = iconColorAssignments[app.packageName]
                                ?: appIconColorStore.getColor(app.packageName),
                            onSelectColor = { color ->
                                appIconColorStore.setColor(app.packageName, color)
                                iconColorAssignments[app.packageName] = color
                            },
                            onDismiss = { colorPickerTargetApp = null },
                        )
                    }

                    if (showHomeKnobSettings) {
                        HomeKnobSettingsMenu(
                            slots = HomeKnobSlot.values().toList(),
                            knobNames = homeKnobNames,
                            assignments = homeKnobAssignments,
                            onNameChange = { slot, name ->
                                homeKnobAssignmentStore.setName(slot, name)
                                homeKnobNames[slot] = name
                            },
                            onEditTapApp = { slot -> startHomeKnobTapPick(slot) },
                            onEditFolder = { slot -> startHomeKnobFolderEdit(slot) },
                            onDismiss = { showHomeKnobSettings = false },
                        )
                    }

                    if (showAppGridSettings) {
                        HomeAppGridSettingsMenu(
                            folders = folders,
                            onAddFolder = {
                                val created = viewModel.addFolder("Folder", emptyList())
                                showAppGridSettings = false
                                folderEditDialogTarget = created.id
                            },
                            onEditFolder = { folder ->
                                showAppGridSettings = false
                                folderEditDialogTarget = folder.id
                            },
                            onDismiss = { showAppGridSettings = false },
                        )
                    }

                    folderEditDialogTarget?.let { targetId ->
                        val folder = folders.firstOrNull { it.id == targetId }
                        if (folder != null) {
                            FolderEditDialog(
                                name = folder.name,
                                appCount = folder.packageNames.size,
                                onNameChange = { newName ->
                                    viewModel.updateFolder(folder.id, newName, folder.packageNames)
                                },
                                onEditApps = { startGridFolderEdit(folder) },
                                onDelete = {
                                    viewModel.deleteFolder(folder.id)
                                    folderEditDialogTarget = null
                                },
                                onDismiss = { folderEditDialogTarget = null },
                            )
                        } else {
                            folderEditDialogTarget = null
                        }
                    }

                    folderOverlay?.let { overlay ->
                        FolderOverlay(
                            name = overlay.name,
                            apps = overlay.apps,
                            onAppClick = { app ->
                                launchApp(app.packageName)
                                folderOverlay = null
                            },
                            onDismiss = { folderOverlay = null },
                        )
                    }


                    if (showLockScreenSyncMenu) {
                        LockScreenSyncMenu(
                            enabled = lockScreenSyncEnabled,
                            onToggle = { enabled ->
                                lockScreenSyncEnabled = enabled
                                lockScreenSyncStore.setEnabled(enabled)
                                if (enabled) applyLockScreenWallpaper()
                            },
                            onDismiss = { showLockScreenSyncMenu = false },
                        )
                    }

                    renameTargetApp?.let { app ->
                        RenameAppDialog(
                            app = app,
                            onConfirm = { newLabel ->
                                viewModel.renameApp(app.packageName, newLabel)
                                renameTargetApp = null
                            },
                            onDismiss = { renameTargetApp = null },
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
        if (lockScreenSyncStore.isEnabled()) applyLockScreenWallpaper()
    }

    private fun applyLockScreenWallpaper() {
        // Read the current display's real bounds on the main thread: on a foldable,
        // WallpaperManager.desiredMinimumWidth/Height can report a size for a
        // different (e.g. unfolded) display than the one actually showing the lock
        // screen, which misplaces the card.
        val bounds = windowManager.currentWindowMetrics.bounds
        val width = bounds.width()
        val height = bounds.height()
        val context = applicationContext
        lifecycleScope.launch(Dispatchers.Default) {
            LockWallpaperGenerator.apply(context, width, height)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemStatusBar()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (shouldCloseDrawerOnNewIntent(intent.action, homeRouteState.value == HomeRoute.DRAWER)) {
            closeDrawer()
        }
    }

    private fun closeDrawer() {
        homeRouteState.value = HomeRoute.HOME
        pendingAppPickState.value = null
        folderEditTargetState.value = null
        viewModel.onQueryChange("")
    }

    private fun initialBrightnessRatio(): Float {
        val current = window.attributes.screenBrightness
        if (current in 0f..1f) return current
        return try {
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255f
        } catch (e: Settings.SettingNotFoundException) {
            0.5f
        }
    }

    private fun setWindowBrightness(ratio: Float) {
        val attrs = window.attributes
        attrs.screenBrightness = ratio.coerceIn(0.01f, 1f)
        window.attributes = attrs
    }

    private fun hideSystemStatusBar() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
    }

    private fun openNotificationListenerSettings() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun launchNowPlayingApp(packageName: String?) {
        if (packageName != null) launchApp(packageName)
    }

    private fun launchCalendarApp() {
        val launchIntent = packageManager.getLaunchIntentForPackage("com.google.android.calendar")
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
            return
        }
        val fallback = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (fallback.resolveActivity(packageManager) != null) {
            startActivity(fallback)
        }
    }

    private fun launchApp(packageName: String) {
        packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun launchYoutubeMusic() = launchApp(YOUTUBE_MUSIC_PACKAGE)

    private fun uninstallApp(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName"))
        startActivity(intent)
    }
}
