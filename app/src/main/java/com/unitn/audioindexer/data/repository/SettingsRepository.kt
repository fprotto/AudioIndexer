package com.unitn.audioindexer.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("audioindexer_prefs", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(
        if (prefs.contains(KEY_DARK_THEME)) prefs.getBoolean(KEY_DARK_THEME, false) else null
    )
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

    private val _equalizerEnabled = MutableStateFlow(prefs.getBoolean(KEY_EQ_ENABLED, false))
    val equalizerEnabled: StateFlow<Boolean> = _equalizerEnabled.asStateFlow()

    private val _equalizerBandLevels = MutableStateFlow(
        prefs.getString(KEY_EQ_BANDS, "")?.split(",")?.filter { it.isNotEmpty() }?.map { it.toInt() } ?: emptyList()
    )
    val equalizerBandLevels: StateFlow<List<Int>> = _equalizerBandLevels.asStateFlow()

    private val _equalizerPreset = MutableStateFlow(prefs.getInt(KEY_EQ_PRESET, -1))
    val equalizerPreset: StateFlow<Int> = _equalizerPreset.asStateFlow()

    private val _equalizerPresetNames = MutableStateFlow(
        prefs.getString(KEY_EQ_PRESET_NAMES, "")?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    )
    val equalizerPresetNames: StateFlow<List<String>> = _equalizerPresetNames.asStateFlow()

    private val _bassBoostEnabled = MutableStateFlow(prefs.getBoolean(KEY_BASS_ENABLED, false))
    val bassBoostEnabled: StateFlow<Boolean> = _bassBoostEnabled.asStateFlow()

    private val _bassBoostStrength = MutableStateFlow(prefs.getInt(KEY_BASS_STRENGTH, 0))
    val bassBoostStrength: StateFlow<Int> = _bassBoostStrength.asStateFlow()

    private val _virtualizerEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIRT_ENABLED, false))
    val virtualizerEnabled: StateFlow<Boolean> = _virtualizerEnabled.asStateFlow()

    private val _virtualizerStrength = MutableStateFlow(prefs.getInt(KEY_VIRT_STRENGTH, 0))
    val virtualizerStrength: StateFlow<Int> = _virtualizerStrength.asStateFlow()

    private val _loudnessEnabled = MutableStateFlow(prefs.getBoolean(KEY_LOUD_ENABLED, false))
    val loudnessEnabled: StateFlow<Boolean> = _loudnessEnabled.asStateFlow()

    private val _loudnessGain = MutableStateFlow(prefs.getInt(KEY_LOUD_GAIN, 0))
    val loudnessGain: StateFlow<Int> = _loudnessGain.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(prefs.getFloat(KEY_PLAYBACK_SPEED, 1.0f))
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    fun setDarkTheme(isDark: Boolean?) {
        _isDarkTheme.value = isDark
        prefs.edit().apply {
            if (isDark == null) {
                remove(KEY_DARK_THEME)
            } else {
                putBoolean(KEY_DARK_THEME, isDark)
            }
            apply()
        }
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        Log.d("SettingsRepository", "setEqualizerEnabled: $enabled")
        _equalizerEnabled.value = enabled
        prefs.edit { putBoolean(KEY_EQ_ENABLED, enabled) }
    }

    fun setEqualizerBandLevels(levels: List<Int>) {
        Log.d("SettingsRepository", "setEqualizerBandLevels: $levels")
        _equalizerBandLevels.value = levels
        prefs.edit { putString(KEY_EQ_BANDS, levels.joinToString(",")) }
    }

    fun setEqualizerPreset(preset: Int) {
        Log.d("SettingsRepository", "setEqualizerPreset: $preset")
        _equalizerPreset.value = preset
        prefs.edit { putInt(KEY_EQ_PRESET, preset) }
    }

    fun setEqualizerPresetNames(names: List<String>) {
        _equalizerPresetNames.value = names
        prefs.edit { putString(KEY_EQ_PRESET_NAMES, names.joinToString(",")) }
    }

    fun setBassBoostEnabled(enabled: Boolean) {
        Log.d("SettingsRepository", "setBassBoostEnabled: $enabled")
        _bassBoostEnabled.value = enabled
        prefs.edit { putBoolean(KEY_BASS_ENABLED, enabled) }
    }

    fun setBassBoostStrength(strength: Int) {
        Log.d("SettingsRepository", "setBassBoostStrength: $strength")
        _bassBoostStrength.value = strength
        prefs.edit { putInt(KEY_BASS_STRENGTH, strength) }
    }

    fun setVirtualizerEnabled(enabled: Boolean) {
        Log.d("SettingsRepository", "setVirtualizerEnabled: $enabled")
        _virtualizerEnabled.value = enabled
        prefs.edit { putBoolean(KEY_VIRT_ENABLED, enabled) }
    }

    fun setVirtualizerStrength(strength: Int) {
        Log.d("SettingsRepository", "setVirtualizerStrength: $strength")
        _virtualizerStrength.value = strength
        prefs.edit { putInt(KEY_VIRT_STRENGTH, strength) }
    }

    fun setLoudnessEnabled(enabled: Boolean) {
        Log.d("SettingsRepository", "setLoudnessEnabled: $enabled")
        _loudnessEnabled.value = enabled
        prefs.edit { putBoolean(KEY_LOUD_ENABLED, enabled) }
    }

    fun setLoudnessGain(gain: Int) {
        Log.d("SettingsRepository", "setLoudnessGain: $gain")
        _loudnessGain.value = gain
        prefs.edit { putInt(KEY_LOUD_GAIN, gain) }
    }

    fun setPlaybackSpeed(speed: Float) {
        Log.d("SettingsRepository", "setPlaybackSpeed: $speed")
        _playbackSpeed.value = speed
        prefs.edit { putFloat(KEY_PLAYBACK_SPEED, speed) }
    }

    companion object {
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_EQ_ENABLED = "eq_enabled"
        private const val KEY_EQ_BANDS = "eq_bands"
        private const val KEY_EQ_PRESET = "eq_preset"
        private const val KEY_EQ_PRESET_NAMES = "eq_preset_names"
        private const val KEY_BASS_ENABLED = "bass_enabled"
        private const val KEY_BASS_STRENGTH = "bass_strength"
        private const val KEY_VIRT_ENABLED = "virt_enabled"
        private const val KEY_VIRT_STRENGTH = "virt_strength"
        private const val KEY_LOUD_ENABLED = "loud_enabled"
        private const val KEY_LOUD_GAIN = "loud_gain"
        private const val KEY_PLAYBACK_SPEED = "playback_speed"
    }
}
