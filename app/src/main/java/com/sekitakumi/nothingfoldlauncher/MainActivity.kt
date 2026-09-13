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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.sekitakumi.nothingfoldlauncher.data.AppInfo
import com.sekitakumi.nothingfoldlauncher.data.AppLabelStore
import com.sekitakumi.nothingfoldlauncher.data.AppRepository
import com.sekitakumi.nothingfoldlauncher.data.FavoritesStore
import com.sekitakumi.nothingfoldlauncher.data.HiddenAppsStore
import com.sekitakumi.nothingfoldlauncher.ui.AppContextMenu
import com.sekitakumi.nothingfoldlauncher.ui.AppDrawer
import com.sekitakumi.nothingfoldlauncher.ui.AppListViewModel
import com.sekitakumi.nothingfoldlauncher.ui.HomeScreen
import com.sekitakumi.nothingfoldlauncher.ui.NowPlayingController
import com.sekitakumi.nothingfoldlauncher.ui.RenameAppDialog
import com.sekitakumi.nothingfoldlauncher.ui.StatusIconsController
import com.sekitakumi.nothingfoldlauncher.ui.VolumeController
import com.sekitakumi.nothingfoldlauncher.ui.theme.NothingFoldLauncherTheme

class MainActivity : ComponentActivity() {

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
                var showDrawer by remember { mutableStateOf(false) }
                val apps by viewModel.visibleApps.collectAsState()
                val homeApps by viewModel.homeApps.collectAsState()
                val favorites by viewModel.favorites.collectAsState()
                val hiddenApps by viewModel.hiddenApps.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()
                val query by viewModel.query.collectAsState()

                var menuTargetApp by remember { mutableStateOf<AppInfo?>(null) }
                var renameTargetApp by remember { mutableStateOf<AppInfo?>(null) }
                val openAppMenu: (AppInfo) -> Unit = { menuTargetApp = it }

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

                val closeDrawer: () -> Unit = {
                    showDrawer = false
                    viewModel.onQueryChange("")
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(showDrawer) {
                            // Open: swipe up / Close: swipe left or swipe down.
                            // Descendant scrollables (the app list, the alphabet
                            // index bar) consume their own drag events, so this
                            // only sees drags that they didn't claim.
                            detectDragGestures(
                                onDragStart = {
                                    dragAccumX = 0f
                                    dragAccumY = 0f
                                },
                                onDragEnd = {
                                    if (showDrawer) {
                                        if (dragAccumX < -120f || dragAccumY > 120f) closeDrawer()
                                    } else {
                                        if (dragAccumY < -120f) showDrawer = true
                                    }
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
                        targetState = showDrawer,
                        transitionSpec = {
                            if (targetState) {
                                // Home -> drawer (swipe up): home exits upward, drawer enters from below
                                slideInVertically { it } togetherWith slideOutVertically { -it }
                            } else {
                                // Drawer -> home: drawer exits downward, home enters from above
                                slideInVertically { -it } togetherWith slideOutVertically { it }
                            }
                        },
                        label = "home-drawer-transition",
                    ) { drawerVisible ->
                        if (drawerVisible) {
                            AppDrawer(
                                apps = apps,
                                query = query,
                                favorites = favorites,
                                onQueryChange = viewModel::onQueryChange,
                                onAppClick = { launchApp(it.packageName) },
                                onAppLongClick = openAppMenu,
                                onSwipeDownToClose = closeDrawer,
                            )
                        } else {
                            HomeScreen(
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
                                onCalendarClick = { launchCalendarApp() },
                                onAppClick = { launchApp(it.packageName) },
                                onAppLongClick = openAppMenu,
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
                            onDismiss = { menuTargetApp = null },
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

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemStatusBar()
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
}
