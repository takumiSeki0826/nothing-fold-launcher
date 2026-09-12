package com.sekitakumi.nothingfoldlauncher.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION"
private const val EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE"

class VolumeController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _ratio = MutableStateFlow(currentRatio())
    val ratio: StateFlow<Float> = _ratio.asStateFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(receivedContext: Context?, intent: Intent?) {
            val streamType = intent?.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1) ?: -1
            if (streamType == AudioManager.STREAM_MUSIC) {
                _ratio.value = currentRatio()
            }
        }
    }

    fun setRatio(newRatio: Float) {
        val manager = audioManager ?: return
        val clamped = newRatio.coerceIn(0f, 1f)
        val maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVolume = (clamped * maxVolume).toInt()
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
        _ratio.value = currentRatio()
    }

    fun register() {
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(VOLUME_CHANGED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        _ratio.value = currentRatio()
    }

    fun unregister() {
        try {
            context.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            // 未登録の場合は何もしない
        }
    }

    private fun currentRatio(): Float {
        val manager = audioManager ?: return 0f
        val max = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (max <= 0) return 0f
        return manager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / max
    }
}
