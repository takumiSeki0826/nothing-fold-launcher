package com.sekitakumi.nothingfoldlauncher.ui

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import androidx.core.app.NotificationManagerCompat
import com.sekitakumi.nothingfoldlauncher.NowPlayingListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NowPlayingState(val title: String, val isPlaying: Boolean, val packageName: String)

class NowPlayingController(private val context: Context) {

    private val _nowPlaying = MutableStateFlow<NowPlayingState?>(null)
    val nowPlaying: StateFlow<NowPlayingState?> = _nowPlaying.asStateFlow()

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()

    private val componentName = ComponentName(context, NowPlayingListenerService::class.java)

    private val mediaSessionManager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager

    private var activeController: MediaController? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateFromController(activeController)
        }

        override fun onMetadataChanged(metadata: android.media.MediaMetadata?) {
            updateFromController(activeController)
        }
    }

    private val sessionsChangedListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            attachTo(controllers?.firstOrNull())
        }

    fun refresh() {
        _permissionGranted.value = isNotificationAccessGranted()
        if (!_permissionGranted.value) {
            _nowPlaying.value = null
            return
        }
        val manager = mediaSessionManager ?: return
        try {
            manager.addOnActiveSessionsChangedListener(sessionsChangedListener, componentName)
            attachTo(manager.getActiveSessions(componentName).firstOrNull())
        } catch (e: SecurityException) {
            _permissionGranted.value = false
            _nowPlaying.value = null
        }
    }

    fun dispose() {
        activeController?.unregisterCallback(controllerCallback)
        activeController = null
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionsChangedListener)
        } catch (e: SecurityException) {
            // 権限が取り消された場合は何もしない
        }
    }

    fun togglePlayPause() {
        val controller = activeController ?: return
        val isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING
        if (isPlaying) {
            controller.transportControls.pause()
        } else {
            controller.transportControls.play()
        }
    }

    private fun attachTo(controller: MediaController?) {
        activeController?.unregisterCallback(controllerCallback)
        activeController = controller
        controller?.registerCallback(controllerCallback)
        updateFromController(controller)
    }

    private fun updateFromController(controller: MediaController?) {
        if (controller == null) {
            _nowPlaying.value = null
            return
        }
        val title = controller.metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)
        val isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING
        _nowPlaying.value = if (title != null) {
            NowPlayingState(title, isPlaying, controller.packageName)
        } else {
            null
        }
    }

    private fun isNotificationAccessGranted(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
}
