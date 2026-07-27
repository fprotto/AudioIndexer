package com.unitn.audioindexer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unitn.audioindexer.data.repository.SettingsRepository
import com.unitn.audioindexer.playback.MusicController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayerSettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val musicController: MusicController
) : ViewModel() {

    val equalizerEnabled = settingsRepository.equalizerEnabled
    val equalizerBandLevels = settingsRepository.equalizerBandLevels
    val equalizerPreset = settingsRepository.equalizerPreset
    val equalizerPresetNames = settingsRepository.equalizerPresetNames
    val bassBoostEnabled = settingsRepository.bassBoostEnabled
    val bassBoostStrength = settingsRepository.bassBoostStrength
    val virtualizerEnabled = settingsRepository.virtualizerEnabled
    val virtualizerStrength = settingsRepository.virtualizerStrength
    val loudnessEnabled = settingsRepository.loudnessEnabled
    val loudnessGain = settingsRepository.loudnessGain
    val playbackSpeed = settingsRepository.playbackSpeed

    fun setEqualizerEnabled(enabled: Boolean) {
        settingsRepository.setEqualizerEnabled(enabled)
    }

    fun setEqualizerBandLevel(index: Int, level: Int) {
        val current = equalizerBandLevels.value.toMutableList()
        while (current.size <= index) {
            current.add(0)
        }
        current[index] = level
        settingsRepository.setEqualizerBandLevels(current)
        settingsRepository.setEqualizerPreset(-1) // Custom
    }

    fun setEqualizerPreset(preset: Int) {
        settingsRepository.setEqualizerPreset(preset)
    }

    fun setBassBoostEnabled(enabled: Boolean) {
        settingsRepository.setBassBoostEnabled(enabled)
    }

    fun setBassBoostStrength(strength: Int) {
        settingsRepository.setBassBoostStrength(strength)
    }

    fun setVirtualizerEnabled(enabled: Boolean) {
        settingsRepository.setVirtualizerEnabled(enabled)
    }

    fun setVirtualizerStrength(strength: Int) {
        settingsRepository.setVirtualizerStrength(strength)
    }

    fun setLoudnessEnabled(enabled: Boolean) {
        settingsRepository.setLoudnessEnabled(enabled)
    }

    fun setLoudnessGain(gain: Int) {
        settingsRepository.setLoudnessGain(gain)
    }

    fun setPlaybackSpeed(speed: Float) {
        settingsRepository.setPlaybackSpeed(speed)
        musicController.setPlaybackSpeed(speed)
    }
}
