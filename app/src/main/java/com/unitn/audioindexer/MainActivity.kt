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
import com.unitn.audioindexer.ui.SettingsViewModel
import com.unitn.audioindexer.ui.navigation.AppNavigation
import com.unitn.audioindexer.ui.theme.AudioIndexerTheme

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Simple seeding for demo purposes
        val repository = (application as AudioIndexerApplication).repository
        lifecycleScope.launch {}

        enableEdgeToEdge()
        setContent {
            AudioIndexerTheme(useDarkTheme = settingsViewModel.isDarkTheme ?: isSystemInDarkTheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
