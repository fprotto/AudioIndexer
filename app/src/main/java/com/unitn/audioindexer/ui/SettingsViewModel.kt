package com.unitn.audioindexer.ui

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    var isDarkTheme by mutableStateOf<Boolean?>(null)
        private set

    fun toggleTheme(systemInDarkTheme: Boolean) {
        isDarkTheme = !(isDarkTheme ?: systemInDarkTheme)
    }

    fun setLanguage(context: Context, languageCode: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(languageCode)
        }
    }
}
