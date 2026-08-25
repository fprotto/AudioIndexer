package com.unitn.audioindexer.ui.screens.mainscreen

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.ui.screens.Screen
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory
import com.unitn.audioindexer.ui.viewmodels.SettingsViewModel

private fun Context.findActivity(): ComponentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun MainScreen(
    navController: NavController,
    state: String,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current.findActivity()!!,
        factory = MusicViewModelFactory(
            (LocalContext.current.applicationContext as AudioIndexerApplication).repository,
            (LocalContext.current.applicationContext as AudioIndexerApplication).musicController,
            (LocalContext.current.applicationContext as AudioIndexerApplication).settingsRepository
        )
    ),
    content: LazyListScope.() -> Unit
) {
    val context = LocalContext.current
    val systemInDarkTheme = isSystemInDarkTheme()
    
    val allSources by settingsViewModel.allSources.collectAsState()
    LaunchedEffect(allSources) {
        if (allSources != null && allSources!!.isEmpty() && navController.currentDestination?.route != Screen.Setup.route) {
            navController.navigate(Screen.Setup.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val sections = listOf("Tracks", "Artists", "Albums", "Playlists")
    val routes = listOf("tracks", "artists", "albums", "playlists")
    val currentIndex = sections.indexOf(state)

    val onSwipeRight = {
        if (currentIndex > 0) {
            navController.navigate(routes[currentIndex - 1]) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val onSwipeLeft = {
        if (currentIndex < routes.size - 1) {
            navController.navigate(routes[currentIndex + 1]) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        modifier = modifier.pointerInput(currentIndex) {
            var offsetX = 0f
            detectHorizontalDragGestures(
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount
                },
                onDragEnd = {
                    if (offsetX > 150) { // Swipe right -> Previous
                        onSwipeRight()
                    } else if (offsetX < -150) { // Swipe left -> Next
                        onSwipeLeft()
                    }
                    offsetX = 0f
                },
                onDragCancel = {
                    offsetX = 0f
                }
            )
        },
        topBar = { 
            TopBar(
                onThemeToggle = { settingsViewModel.toggleTheme(systemInDarkTheme) },
                onLanguageChange = { lang -> settingsViewModel.setLanguage(context, lang) },
                languages = settingsViewModel.getSupportedLanguages(context),
                settingsViewModel = settingsViewModel,
                onAddProfile = { navController.navigate(Screen.Setup.route) },
                onPlayerSettingsClick = { navController.navigate(Screen.PlayerSettings.route) }
            ) 
        },
        bottomBar = { 
            MiniPlayer(
                onClick = { navController.navigate(Screen.Player.route) }
            ) 
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            stickyHeader {
                QuickNavigateToSection(
                    navController = navController,
                    currentSection = state
                )
            }
            content()
        }
    }
}
