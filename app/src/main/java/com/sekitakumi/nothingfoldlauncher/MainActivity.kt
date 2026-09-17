package com.sekitakumi.nothingfoldlauncher

import android.Manifest
import android.content.Intent
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
import com.sekitakumi.nothingfoldlauncher.data.AppIconColorStore
import com.sekitakumi.nothingfoldlauncher.data.AppInfo
import com.sekitakumi.nothingfoldlauncher.data.AppLabelStore
import com.sekitakumi.nothingfoldlauncher.data.AppRepository
import com.sekitakumi.nothingfoldlauncher.data.EqKnobAssignmentStore
import com.sekitakumi.nothingfoldlauncher.data.FavoritesStore
import com.sekitakumi.nothingfoldlauncher.data.HiddenAppsStore
import com.sekitakumi.nothingfoldlauncher.data.HomeKnobAssignmentStore
import com.sekitakumi.nothingfoldlauncher.data.LockScreenSyncStore
import com.sekitakumi.nothingfoldlauncher.ui.AppContextMenu
import com.sekitakumi.nothingfoldlauncher.ui.AppDrawer
import com.sekitakumi.nothingfoldlauncher.ui.AppListViewModel
import com.sekitakumi.nothingfoldlauncher.ui.EqKnobSettingsMenu
import com.sekitakumi.nothingfoldlauncher.ui.EqScreen
import com.sekitakumi.nothingfoldlauncher.ui.HomeKnobAppAssignment
import com.sekitakumi.nothingfoldlauncher.ui.HomeKnobSettingsMenu
import com.sekitakumi.nothingfoldlauncher.ui.HomeKnobSlot
import com.sekitakumi.nothingfoldlauncher.ui.HomeRoute
import com.sekitakumi.nothingfoldlauncher.ui.HomeScreen
import com.sekitakumi.nothingfoldlauncher.ui.IconColorPickerDialog
import com.sekitakumi.nothingfoldlauncher.ui.IconPaletteColor
import com.sekitakumi.nothingfoldlauncher.ui.KnobAppAssignment
import com.sekitakumi.nothingfoldlauncher.ui.KnobSlot
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

private sealed class PendingKnobPick {
    data class Eq(val slot: KnobSlot, val isLongPress: Boolean) : PendingKnobPick()
    data class Home(val slot: HomeKnobSlot, val isLongPress: Boolean) : PendingKnobPick()
}

class MainActivity : ComponentActivity() {

    private val homeRouteState = mutableStateOf(HomeRoute.HOME)
    private val pendingKnobPickState = mutableStateOf<PendingKnobPick?>(null)

    private val lockScreenSyncStore by lazy { LockScreenSyncStore(applicationContext) }
    private val eqKnobAssignmentStore by lazy { EqKnobAssignmentStore(applicationContext) }
    private val homeKnobAssignmentStore by lazy { HomeKnobAssignmentStore(applicationContext) }
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
                var pendingKnobPick by pendingKnobPickState
                val isExpandedWidth = isExpandedWidth(LocalConfiguration.current.screenWidthDp)
                val apps by viewModel.visibleApps.collectAsState()
                val homeApps by viewModel.homeApps.collectAsState()
                val favorites by viewModel.favorites.collectAsState()
                val hiddenApps by viewModel.hiddenApps.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()
                val query by viewModel.query.collectAsState()

                var menuTargetApp by remember { mutableStateOf<AppInfo?>(null) }
                var renameTargetApp by remember { mutableStateOf<AppInfo?>(null) }
                val openAppMenu: (AppInfo) -> Unit = { menuTargetApp = it }

                var showLockScreenSyncMenu by remember { mutableStateOf(false) }
                var lockScreenSyncEnabled by remember { mutableStateOf(lockScreenSyncStore.isEnabled()) }
                var showEqKnobSettings by remember { mutableStateOf(false) }
                var showHomeKnobSettings by remember { mutableStateOf(false) }
                var colorPickerTargetApp by remember { mutableStateOf<AppInfo?>(null) }
                val iconColorAssignments = remember { mutableStateMapOf<String, IconPaletteColor>() }

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
                val knobPackageAssignments = remember {
                    mutableStateMapOf<KnobSlot, Pair<String?, String?>>().apply {
                        KnobSlot.values().forEach { slot ->
                            put(
                                slot,
                                eqKnobAssignmentStore.getTapPackage(slot) to
                                    eqKnobAssignmentStore.getLongPressPackage(slot),
                            )
                        }
                    }
                }
                val knobAssignments = KnobSlot.values().associateWith { slot ->
                    val (tapPackage, longPressPackage) = knobPackageAssignments[slot] ?: (null to null)
                    KnobAppAssignment(
                        tapApp = tapPackage?.let(appsByPackage::get),
                        longPressApp = longPressPackage?.let(appsByPackage::get),
                    )
                }

                val knobNames = remember {
                    mutableStateMapOf<KnobSlot, String>().apply {
                        KnobSlot.values().forEach { slot ->
                            eqKnobAssignmentStore.getName(slot)?.let { put(slot, it) }
                        }
                    }
                }

                val homeKnobPackageAssignments = remember {
                    mutableStateMapOf<HomeKnobSlot, Pair<String?, String?>>().apply {
                        HomeKnobSlot.values().forEach { slot ->
                            put(
                                slot,
                                homeKnobAssignmentStore.getTapPackage(slot) to
                                    homeKnobAssignmentStore.getLongPressPackage(slot),
                            )
                        }
                    }
                }
                val homeKnobAssignments = HomeKnobSlot.values().associateWith { slot ->
                    val (tapPackage, longPressPackage) = homeKnobPackageAssignments[slot] ?: (null to null)
                    HomeKnobAppAssignment(
                        tapApp = tapPackage?.let(appsByPackage::get),
                        longPressApp = longPressPackage?.let(appsByPackage::get),
                    )
                }
                val homeKnobNames = remember {
                    mutableStateMapOf<HomeKnobSlot, String>().apply {
                        HomeKnobSlot.values().forEach { slot ->
                            homeKnobAssignmentStore.getName(slot)?.let { put(slot, it) }
                        }
                    }
                }

                val assignPendingKnobApp: (AppInfo) -> Unit = { app ->
                    when (val pending = pendingKnobPick) {
                        is PendingKnobPick.Eq -> {
                            val current = knobPackageAssignments[pending.slot] ?: (null to null)
                            if (pending.isLongPress) {
                                eqKnobAssignmentStore.setLongPressPackage(pending.slot, app.packageName)
                                knobPackageAssignments[pending.slot] = current.first to app.packageName
                            } else {
                                eqKnobAssignmentStore.setTapPackage(pending.slot, app.packageName)
                                knobPackageAssignments[pending.slot] = app.packageName to current.second
                            }
                            homeRoute = HomeRoute.EQ
                        }
                        is PendingKnobPick.Home -> {
                            val current = homeKnobPackageAssignments[pending.slot] ?: (null to null)
                            if (pending.isLongPress) {
                                homeKnobAssignmentStore.setLongPressPackage(pending.slot, app.packageName)
                                homeKnobPackageAssignments[pending.slot] = current.first to app.packageName
                            } else {
                                homeKnobAssignmentStore.setTapPackage(pending.slot, app.packageName)
                                homeKnobPackageAssignments[pending.slot] = app.packageName to current.second
                            }
                            homeRoute = HomeRoute.HOME
                        }
                        null -> Unit
                    }
                    pendingKnobPick = null
                }

                val onDrawerDismissed: () -> Unit = {
                    when (pendingKnobPick) {
                        is PendingKnobPick.Eq -> {
                            pendingKnobPick = null
                            homeRoute = HomeRoute.EQ
                        }
                        is PendingKnobPick.Home, null -> {
                            pendingKnobPick = null
                            closeDrawer()
                        }
                    }
                }

                val startEqKnobAppPick: (KnobSlot, Boolean) -> Unit = { slot, isLongPress ->
                    showEqKnobSettings = false
                    pendingKnobPick = PendingKnobPick.Eq(slot, isLongPress)
                    homeRoute = HomeRoute.DRAWER
                }

                val startHomeKnobAppPick: (HomeKnobSlot, Boolean) -> Unit = { slot, isLongPress ->
                    showHomeKnobSettings = false
                    pendingKnobPick = PendingKnobPick.Home(slot, isLongPress)
                    homeRoute = HomeRoute.DRAWER
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(homeRoute) {
                            // Home: swipe up opens the drawer, swipe left opens the EQ screen.
                            // Drawer: swipe left or down returns home. EQ: swipe right returns home.
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
                                    val cancelingDrawer = homeRoute == HomeRoute.DRAWER && nextRoute == HomeRoute.HOME
                                    homeRoute = if (cancelingDrawer && pendingKnobPick is PendingKnobPick.Eq) {
                                        pendingKnobPick = null
                                        HomeRoute.EQ
                                    } else {
                                        if (cancelingDrawer) pendingKnobPick = null
                                        nextRoute
                                    }
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
                                initialState == HomeRoute.DRAWER && targetState == HomeRoute.HOME ->
                                    // Drawer -> home: drawer exits downward, home enters from above
                                    slideInVertically { -it } togetherWith slideOutVertically { it }
                                initialState == HomeRoute.HOME && targetState == HomeRoute.EQ ->
                                    // Home -> EQ (swipe left): home exits left, EQ enters from the right
                                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                                else ->
                                    // EQ -> home (swipe right): EQ exits right, home enters from the left
                                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                            }
                        },
                        label = "home-route-transition",
                    ) { route ->
                        when (route) {
                            HomeRoute.DRAWER -> AppDrawer(
                                apps = apps,
                                query = query,
                                favorites = favorites,
                                onQueryChange = viewModel::onQueryChange,
                                onAppClick = { app ->
                                    if (pendingKnobPick != null) assignPendingKnobApp(app) else launchApp(app.packageName)
                                },
                                onAppLongClick = { app ->
                                    if (pendingKnobPick != null) assignPendingKnobApp(app) else openAppMenu(app)
                                },
                                onSwipeDownToClose = onDrawerDismissed,
                                isExpandedWidth = isExpandedWidth,
                            )
                            HomeRoute.EQ -> EqScreen(
                                knobNames = knobNames,
                                onKnobTap = { slot ->
                                    val tapPackage = knobPackageAssignments[slot]?.first
                                    if (tapPackage != null) {
                                        launchApp(tapPackage)
                                    } else {
                                        startEqKnobAppPick(slot, false)
                                    }
                                },
                                onKnobLongPress = { slot ->
                                    val longPressPackage = knobPackageAssignments[slot]?.second
                                    if (longPressPackage != null) {
                                        launchApp(longPressPackage)
                                    } else {
                                        startEqKnobAppPick(slot, true)
                                    }
                                },
                                onBackgroundLongPress = { showEqKnobSettings = true },
                            )
                            HomeRoute.HOME -> HomeScreen(
                                apps = homeApps,
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
                                isExpandedWidth = isExpandedWidth,
                                homeKnobNames = homeKnobNames,
                                onHomeKnobTap = { slot ->
                                    val tapPackage = homeKnobPackageAssignments[slot]?.first
                                    if (tapPackage != null) {
                                        launchApp(tapPackage)
                                    } else {
                                        startHomeKnobAppPick(slot, false)
                                    }
                                },
                                onHomeKnobLongPress = { slot ->
                                    val longPressPackage = homeKnobPackageAssignments[slot]?.second
                                    if (longPressPackage != null) {
                                        launchApp(longPressPackage)
                                    } else {
                                        startHomeKnobAppPick(slot, true)
                                    }
                                },
                                onHomeKnobSettingsLongPress = { showHomeKnobSettings = true },
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

                    if (showEqKnobSettings) {
                        EqKnobSettingsMenu(
                            slots = KnobSlot.values().toList(),
                            knobNames = knobNames,
                            assignments = knobAssignments,
                            onNameChange = { slot, name ->
                                eqKnobAssignmentStore.setName(slot, name)
                                knobNames[slot] = name
                            },
                            onEditTapApp = { slot -> startEqKnobAppPick(slot, false) },
                            onEditLongPressApp = { slot -> startEqKnobAppPick(slot, true) },
                            onDismiss = { showEqKnobSettings = false },
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
                            onEditTapApp = { slot -> startHomeKnobAppPick(slot, false) },
                            onEditLongPressApp = { slot -> startHomeKnobAppPick(slot, true) },
                            onDismiss = { showHomeKnobSettings = false },
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
        pendingKnobPickState.value = null
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
}
