package com.unitn.audioindexer.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("audioindexer_prefs", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(
        if (prefs.contains(KEY_DARK_THEME)) prefs.getBoolean(KEY_DARK_THEME, false) else null
    )
    val isDarkTheme: StateFlow<Boolean?> = _isDarkTheme.asStateFlow()

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

    companion object {
        private const val KEY_DARK_THEME = "dark_theme"
    }
}
