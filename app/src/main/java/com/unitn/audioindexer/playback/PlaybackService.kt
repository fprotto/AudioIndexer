package com.unitn.audioindexer.playback

import android.content.Intent
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.unitn.audioindexer.AudioIndexerApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player).build()

        setupAudioEffects(player.audioSessionId)
    }

    private fun setupAudioEffects(audioSessionId: Int) {
        val app = application as AudioIndexerApplication
        val settings = app.settingsRepository

        Log.d("PlaybackService", "Setting up audio effects for session: $audioSessionId")

        try {
            equalizer = Equalizer(0, audioSessionId)
            bassBoost = BassBoost(0, audioSessionId)
            virtualizer = Virtualizer(0, audioSessionId)
            loudnessEnhancer = LoudnessEnhancer(audioSessionId)

            Log.d("PlaybackService", "Equalizer bands: ${equalizer?.numberOfBands}")

            val numPresets = equalizer?.numberOfPresets ?: 0
            if (numPresets > 0) {
                val names = List(numPresets.toInt()) { i ->
                    equalizer?.getPresetName(i.toShort()) ?: "Preset $i"
                }
                settings.setEqualizerPresetNames(names)
            }
        } catch (e: Exception) {
            Log.e("PlaybackService", "Failed to initialize audio effects", e)
        }

        try {
            val numBands = equalizer?.numberOfBands ?: 0
            if (settings.equalizerBandLevels.value.isEmpty() && numBands > 0) {
                Log.d("PlaybackService", "Initializing default band levels for $numBands bands")
                settings.setEqualizerBandLevels(List(numBands.toInt()) { 0 })
            }

            serviceScope.launch {
                settings.equalizerEnabled.collect { enabled ->
                    Log.d("PlaybackService", "Equalizer enabled: $enabled")
                    equalizer?.enabled = enabled
                }
            }

            serviceScope.launch {
                settings.equalizerPreset.collect { presetIndex ->
                    if (presetIndex >= 0 && presetIndex < (equalizer?.numberOfPresets ?: 0)) {
                        Log.d("PlaybackService", "Using equalizer preset: $presetIndex")
                        try {
                            equalizer?.usePreset(presetIndex.toShort())
                            // Update repository with new levels from preset
                            val numBands = equalizer?.numberOfBands ?: 0
                            val levels = List(numBands.toInt()) { i ->
                                equalizer?.getBandLevel(i.toShort())?.toInt() ?: 0
                            }
                            settings.setEqualizerBandLevels(levels)
                        } catch (e: Exception) {
                            Log.e("PlaybackService", "Failed to use preset $presetIndex", e)
                        }
                    }
                }
            }

            serviceScope.launch {
                settings.equalizerBandLevels.collect { levels ->
                    Log.d("PlaybackService", "Applying band levels: $levels")
                    levels.forEachIndexed { index, level ->
                        if (index < (equalizer?.numberOfBands ?: 0)) {
                            try {
                                equalizer?.setBandLevel(index.toShort(), level.toShort())
                            } catch (e: Exception) {
                                Log.e("PlaybackService", "Failed to set band level for index $index", e)
                            }
                        }
                    }
                }
            }

            serviceScope.launch {
                settings.bassBoostEnabled.collect { enabled ->
                    Log.d("PlaybackService", "BassBoost enabled: $enabled")
                    bassBoost?.enabled = enabled
                }
            }

            serviceScope.launch {
                settings.bassBoostStrength.collect { strength ->
                    Log.d("PlaybackService", "BassBoost strength: $strength")
                    if (bassBoost?.strengthSupported == true) {
                        bassBoost?.setStrength(strength.toShort())
                    }
                }
            }

            serviceScope.launch {
                settings.virtualizerEnabled.collect { enabled ->
                    Log.d("PlaybackService", "Virtualizer enabled: $enabled")
                    virtualizer?.enabled = enabled
                }
            }

            serviceScope.launch {
                settings.virtualizerStrength.collect { strength ->
                    Log.d("PlaybackService", "Virtualizer strength: $strength")
                    if (virtualizer?.strengthSupported == true) {
                        virtualizer?.setStrength(strength.toShort())
                    }
                }
            }

            serviceScope.launch {
                settings.loudnessEnabled.collect { enabled ->
                    Log.d("PlaybackService", "Loudness enhancer enabled: $enabled")
                    loudnessEnhancer?.enabled = enabled
                }
            }

            serviceScope.launch {
                settings.loudnessGain.collect { gain ->
                    Log.d("PlaybackService", "Loudness enhancer gain: $gain")
                    loudnessEnhancer?.setTargetGain(gain)
                }
            }

            serviceScope.launch {
                settings.playbackSpeed.collect { speed ->
                    Log.d("PlaybackService", "Playback speed: $speed")
                    mediaSession?.player?.setPlaybackSpeed(speed)
                }
            }
        } catch (e: Exception) {
            Log.e("PlaybackService", "Error in audio effect collection", e)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        loudnessEnhancer?.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    @OptIn(UnstableApi::class)
    override fun onTaskRemoved(rootIntent: Intent?) {
        val app = application as AudioIndexerApplication
        app.musicController.destroyPlayer()

        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        stopSelf()
    }
}
