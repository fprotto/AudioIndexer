package com.unitn.audioindexer.ui.screens.mainscreen

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.unitn.audioindexer.R
import com.unitn.audioindexer.data.database.entities.MusicSourceEntity
import com.unitn.audioindexer.ui.screens.setup.RemoteSetupDialog
import com.unitn.audioindexer.ui.viewmodels.SettingsViewModel

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

                    HorizontalDivider()

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
