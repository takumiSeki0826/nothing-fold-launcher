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

data class NowPlayingState(
    val title: String,
    val artist: String?,
    val isPlaying: Boolean,
    val packageName: String,
)

class NowPlayingController(private val context: Context) {

    private val _nowPlaying = MutableStateFlow<NowPlayingState?>(null)
    val nowPlaying: StateFlow<NowPlayingState?> = _nowPlaying.asStateFlow()

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()

    private val componentName = ComponentName(context, NowPlayingListenerService::class.java)

    private val mediaSessionManager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager

    private var sessions: List<MediaController> = emptyList()
    private var activeController: MediaController? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            reselect()
        }

        override fun onMetadataChanged(metadata: android.media.MediaMetadata?) {
            reselect()
        }
    }

    private val sessionsChangedListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            setSessions(controllers.orEmpty())
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
            setSessions(manager.getActiveSessions(componentName))
        } catch (e: SecurityException) {
            _permissionGranted.value = false
            _nowPlaying.value = null
        }
    }

    fun dispose() {
        sessions.forEach { it.unregisterCallback(controllerCallback) }
        sessions = emptyList()
        activeController = null
        try {
            mediaSessionManager?.removeOnActiveSessionsChangedListener(sessionsChangedListener)
        } catch (e: SecurityException) {
            // No-op if permission was already revoked
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

    /** 全セッションの再生状態を監視し、再生中のものを優先して表示対象にする。 */
    private fun setSessions(controllers: List<MediaController>) {
        sessions.forEach { it.unregisterCallback(controllerCallback) }
        sessions = controllers
        sessions.forEach { it.registerCallback(controllerCallback) }
        reselect()
    }

    private fun reselect() {
        activeController = pickNowPlayingSession(
            sessions,
            { it.packageName },
            { it.playbackState?.state == PlaybackState.STATE_PLAYING },
        )
        updateFromController(activeController)
    }

    private fun updateFromController(controller: MediaController?) {
        if (controller == null) {
            _nowPlaying.value = null
            return
        }
        val metadata = controller.metadata
        val title = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE)
        val artist = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
        val isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING
        _nowPlaying.value = if (title != null) {
            NowPlayingState(title, artist, isPlaying, controller.packageName)
        } else {
            null
        }
    }

    private fun isNotificationAccessGranted(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
}
