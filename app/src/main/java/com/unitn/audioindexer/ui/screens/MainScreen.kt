package com.unitn.audioindexer.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChangeCircle
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unitn.audioindexer.data.database.entities.MusicSourceEntity
import com.unitn.audioindexer.ui.screens.setup.RemoteSetupDialog
import androidx.core.os.ConfigurationCompat
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.R
import com.unitn.audioindexer.ui.viewmodels.MiniPlayerViewModel
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

@SuppressLint("LocalContextConfigurationRead")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onThemeToggle: () -> Unit,
    onLanguageChange: (String) -> Unit,
    languages: List<Pair<String, String>>,
    settingsViewModel: SettingsViewModel,
    onAddProfile: () -> Unit,
    onPlayerSettingsClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<MusicSourceEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<MusicSourceEntity?>(null) }
    var showResyncDialog by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.let { stream ->
                settingsViewModel.exportConfiguration(stream) {
                    Toast.makeText(context, R.string.config_exported, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.let { stream ->
                settingsViewModel.importConfiguration(stream) { result ->
                    result.fold(
                        onSuccess = {
                            Toast.makeText(context, R.string.config_imported, Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { error ->
                            Toast.makeText(context, error.message ?: "Import failed", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        }
    }

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
                            onPlayerSettingsClick()
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
                                Icons.Default.Download,
                                contentDescription = stringResource(R.string.settings_import_config)
                            )
                        },
                        text = { Text(stringResource(R.string.settings_import_config)) },
                        onClick = {
                            showMenu = false
                            importLauncher.launch(arrayOf("application/json"))
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                Icons.Default.Upload,
                                contentDescription = stringResource(R.string.settings_export_config)
                            )
                        },
                        text = { Text(stringResource(R.string.settings_export_config)) },
                        onClick = {
                            showMenu = false
                            exportLauncher.launch("audioindexer_config.json")
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
                            showMenu = false
                            showProfileMenu = true
                        }
                    )
                }

                DropdownMenu(
                    expanded = showProfileMenu,
                    onDismissRequest = { showProfileMenu = false }
                ) {
                    val sources by settingsViewModel.allSources.collectAsState()
                    val activeId by settingsViewModel.activeSourceId.collectAsState()

                    sources?.forEach { source ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = source.name,
                                    fontWeight = if (activeId == source.id) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            trailingIcon = {
                                Row {
                                    IconButton(onClick = {
                                        settingsViewModel.syncSource(source.id)
                                        showProfileMenu = false
                                    }) {
                                        Icon(
                                            Icons.Default.Sync,
                                            contentDescription = stringResource(R.string.force_sync)
                                        )
                                    }
                                    IconButton(onClick = {
                                        showProfileMenu = false
                                        showEditDialog = source
                                    }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = stringResource(R.string.edit_profile)
                                        )
                                    }
                                    IconButton(onClick = {
                                        showProfileMenu = false
                                        showDeleteDialog = source
                                    }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.menu_delete),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            },
                            onClick = {
                                settingsViewModel.setActiveSource(source.id)
                                showProfileMenu = false
                            }
                        )
                    }
                    
                    androidx.compose.material3.HorizontalDivider()
                    
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.add_profile)
                            )
                        },
                        text = { Text(stringResource(R.string.add_profile)) },
                        onClick = {
                            showProfileMenu = false
                            onAddProfile()
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

    showEditDialog?.let { source ->
        RemoteSetupDialog(
            initialSource = source,
            onDismiss = { showEditDialog = null },
            onSave = { name, ip, port ->
                settingsViewModel.updateSource(
                    source.copy(name = name, path = ip, port = port),
                    source,
                    onConfirmResync = { showResyncDialog = source.id }
                )
                showEditDialog = null
            }
        )
    }

    showDeleteDialog?.let { source ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.delete_profile_title)) },
            text = { Text(stringResource(R.string.delete_profile_confirmation, source.name)) },
            confirmButton = {
                TextButton(onClick = {
                    settingsViewModel.deleteSource(source)
                    showDeleteDialog = null
                }) {
                    Text(stringResource(R.string.menu_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    showResyncDialog?.let { sourceId ->
        AlertDialog(
            onDismissRequest = { showResyncDialog = null },
            title = { Text(stringResource(R.string.resync_title)) },
            text = { Text(stringResource(R.string.resync_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    settingsViewModel.clearSongsForSource(sourceId)
                    showResyncDialog = null
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    settingsViewModel.syncSource(sourceId)
                    showResyncDialog = null 
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun MiniPlayer(
    onClick: () -> Unit,
    viewModel: MiniPlayerViewModel = viewModel(
        factory = MusicViewModelFactory(
            (LocalContext.current.applicationContext as AudioIndexerApplication).repository,
            (LocalContext.current.applicationContext as AudioIndexerApplication).musicController,
            (LocalContext.current.applicationContext as AudioIndexerApplication).settingsRepository
        )
    )
) {
    val playbackState by viewModel.state.collectAsState()
    val song = playbackState.currentSong

    Box(modifier = Modifier.navigationBarsPadding()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .pointerInput(Unit) {
                    var offsetY = 0f
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            offsetY += dragAmount
                        },
                        onDragEnd = {
                            if (offsetY < -50) { // Swipe up -> Open Player
                                onClick()
                            }
                            offsetY = 0f
                        },
                        onDragCancel = {
                            offsetY = 0f
                        }
                    )
                }
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
                if (song != null) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artistName,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.no_song_playing),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.skipPrevious() }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = null)
                    }
                    IconButton(onClick = { viewModel.togglePlayPause() }) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = { viewModel.skipNext() }) {
                        Icon(Icons.Default.SkipNext, contentDescription = null)
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
            .background(MaterialTheme.colorScheme.surfaceContainer),
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
