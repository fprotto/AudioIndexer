package com.unitn.audioindexer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.unitn.audioindexer.ui.components.navigation.AppNavigation
import com.unitn.audioindexer.ui.screens.Screen
import com.unitn.audioindexer.ui.theme.AudioIndexerTheme
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory
import com.unitn.audioindexer.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels {
        val app = application as AudioIndexerApplication
        MusicViewModelFactory(app.repository, app.musicController, app.settingsRepository)
    }
    private var startDestination by mutableStateOf<String?>(null)
    private var navigateToPlayer by mutableStateOf(false)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("OPEN_PLAYER", false)) {
            navigateToPlayer = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.getBooleanExtra("OPEN_PLAYER", false)) {
            navigateToPlayer = true
        }

        val app = application as AudioIndexerApplication
        app.musicController.initialize()

        val repository = app.repository
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
            val isDarkTheme by settingsViewModel.isDarkTheme.collectAsState()
            val navController = rememberNavController()

            LaunchedEffect(navigateToPlayer) {
                if (navigateToPlayer) {
                    navController.navigate(Screen.Player.route) {
                        launchSingleTop = true
                    }
                    navigateToPlayer = false
                }
            }
            
            if (destination != null) {
                AudioIndexerTheme(useDarkTheme = isDarkTheme ?: isSystemInDarkTheme()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(
                            navController = navController,
                            startDestination = destination
                        )
                    }
                }
            }
        }
    }
}
