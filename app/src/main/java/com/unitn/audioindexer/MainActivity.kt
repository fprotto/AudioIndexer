package com.unitn.audioindexer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory
import com.unitn.audioindexer.ui.viewmodels.SettingsViewModel
import com.unitn.audioindexer.ui.navigation.AppNavigation
import com.unitn.audioindexer.ui.theme.AudioIndexerTheme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.unitn.audioindexer.ui.screens.Screen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels {
        val app = application as AudioIndexerApplication
        MusicViewModelFactory(app.repository, app.musicController)
    }
    private var startDestination by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as AudioIndexerApplication).repository
        lifecycleScope.launch {
            val count = repository.getSourceCount()
            if (count > 0) {
                // Set the first source as active by default
                val firstSource = repository.allSources.first().firstOrNull()
                repository.setActiveSource(firstSource?.id)
                startDestination = Screen.Tracks.route
            } else {
                startDestination = Screen.Setup.route
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.activeSourceId.collect { id ->
                    if (id != null) {
                        repository.syncSource(id)
                    }
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            val destination = startDestination
            if (destination != null) {
                AudioIndexerTheme(useDarkTheme = settingsViewModel.isDarkTheme ?: isSystemInDarkTheme()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(startDestination = destination)
                    }
                }
            }
        }
    }
}
