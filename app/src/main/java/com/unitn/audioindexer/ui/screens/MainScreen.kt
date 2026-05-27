package com.unitn.audioindexer.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unitn.audioindexer.R
import com.unitn.audioindexer.ui.SettingsViewModel

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
    sampleState: String, // FIXME: to remove once the data layer is implemented
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current.findActivity()!!
    ),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDarkTheme = isSystemInDarkTheme()
    Scaffold(
        modifier = modifier,
        topBar = { 
            TopBar(
                onThemeToggle = { settingsViewModel.toggleTheme(systemInDarkTheme) },
                onLanguageChange = { lang -> settingsViewModel.setLanguage(context, lang) },
                languages = settingsViewModel.getSupportedLanguages(context)
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
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { 
                QuickNavigateToSection(
                    navController = navController, 
                    currentSection = sampleState
                ) 
            }
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    content()
                }
            }
        }
    }
}

@SuppressLint("LocalContextConfigurationRead")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onThemeToggle: () -> Unit,
    onLanguageChange: (String) -> Unit,
    languages: List<Pair<String, String>>
) {
    var showMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    TopAppBar(
        title = {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.height(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        actions = {
            Box {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                Icons.Default.SettingsInputComponent,
                                contentDescription = stringResource(R.string.settings_player_options)
                            )
                        },
                        text = { Text(stringResource(R.string.settings_player_options)) },
                        onClick = {
                            /* TODO: implement */
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                Icons.Default.Language,
                                contentDescription = stringResource(R.string.settings_change_language)
                            )
                        },
                        text = { Text(stringResource(R.string.settings_change_language)) },
                        onClick = {
                            showMenu = false
                            showLanguageMenu = true
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                Icons.Default.InvertColors,
                                contentDescription = stringResource(R.string.settings_switch_theme)
                            )
                        },
                        text = { Text(stringResource(R.string.settings_switch_theme)) },
                        onClick = {
                            onThemeToggle()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                Icons.Default.ImportExport,
                                contentDescription = stringResource(R.string.settings_export_config)
                            )
                        },
                        text = { Text(stringResource(R.string.settings_export_config)) },
                        onClick = {
                            /* TODO: implement */
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                Icons.Default.ChangeCircle,
                                contentDescription = stringResource(R.string.settings_switch_profile)
                            )
                        },
                        text = { Text(stringResource(R.string.settings_switch_profile)) },
                        onClick = {
                            /* TODO: implement */
                            showMenu = false
                        }
                    )
                }

                DropdownMenu(
                    expanded = showLanguageMenu,
                    onDismissRequest = { showLanguageMenu = false }
                ) {
                    val currentLocale = ConfigurationCompat.getLocales(context.resources.configuration)[0]?.language ?: "en"
                    languages.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = name,
                                    fontWeight = if (currentLocale == code) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                onLanguageChange(code)
                                showLanguageMenu = false
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun MiniPlayer(onClick: () -> Unit) {
    Box(modifier = Modifier.navigationBarsPadding()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable(onClick = onClick),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.no_song_playing))

                Row {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickNavigateToSection(
    navController: NavController,
    currentSection: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavigationTab(
            label = stringResource(R.string.tracks_header),
            isSelected = currentSection == "Tracks",
            onClick = { navController.navigate("tracks") },
            modifier = Modifier.weight(1f)
        )
        NavigationTab(
            label = stringResource(R.string.artists_header),
            isSelected = currentSection == "Artists",
            onClick = { navController.navigate("artists") },
            modifier = Modifier.weight(1f)
        )
        NavigationTab(
            label = stringResource(R.string.albums_header),
            isSelected = currentSection == "Albums",
            onClick = { navController.navigate("albums") },
            modifier = Modifier.weight(1f)
        )
        NavigationTab(
            label = stringResource(R.string.playlists_header),
            isSelected = currentSection == "Playlists",
            onClick = { navController.navigate("playlists") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun NavigationTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.labelMedium
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(contentColor)
            )
        }
    }
}
