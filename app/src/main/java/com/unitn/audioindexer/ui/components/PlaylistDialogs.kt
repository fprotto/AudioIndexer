package com.unitn.audioindexer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.unitn.audioindexer.R
import com.unitn.audioindexer.data.components.Playlist
import com.unitn.audioindexer.data.components.Song

@Composable
fun CreatePlaylistDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.new_playlist)) },
        text = {
            OutlinedTextField(
                value = playlistName,
                onValueChange = { playlistName = it },
                label = { Text(stringResource(R.string.title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (playlistName.isNotBlank()) {
                        onConfirm(playlistName)
                    }
                },
                enabled = playlistName.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun AddToPlaylistDialog(
    playlists: List<Playlist>,
    onDismissRequest: () -> Unit,
    onPlaylistSelected: (Playlist) -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.menu_add_to_playlist)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.new_playlist)) },
                    leadingContent = {
                        Icon(Icons.AutoMirrored.Filled.FeaturedPlayList, contentDescription = null)
                    },
                    modifier = Modifier.clickable { onCreateNewPlaylist() }
                )
                
                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(playlists) { playlist ->
                        ListItem(
                            headlineContent = { Text(playlist.name) },
                            modifier = Modifier.clickable { onPlaylistSelected(playlist) }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun EditPlaylistDialog(
    initialName: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.menu_rename)) },
        text = {
            OutlinedTextField(
                value = playlistName,
                onValueChange = { playlistName = it },
                label = { Text(stringResource(R.string.title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (playlistName.isNotBlank()) {
                        onConfirm(playlistName)
                    }
                },
                enabled = playlistName.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun DeletePlaylistConfirmationDialog(
    playlistName: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.menu_delete_playlist)) },
        text = {
            Text(stringResource(R.string.playlist_deletion_confirmation, playlistName))
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.menu_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun SongSelectionDialog(
    allSongs: List<Song>,
    onDismissRequest: () -> Unit,
    onConfirm: (List<Song>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedSongs = remember { mutableStateListOf<Song>() }

    val filteredSongs by remember(searchQuery, allSongs) {
        derivedStateOf {
            allSongs.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artistName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.select_songs)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    placeholder = {
                        Text(
                            stringResource(R.string.search_songs),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                )

                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(filteredSongs) { song ->
                        val isSelected = selectedSongs.contains(song)
                        ListItem(
                            headlineContent = { Text(song.title) },
                            supportingContent = { Text(song.artistName) },
                            trailingContent = {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedSongs.add(song)
                                        else selectedSongs.remove(song)
                                    }
                                )
                            },
                            modifier = Modifier.clickable {
                                if (isSelected) selectedSongs.remove(song)
                                else selectedSongs.add(song)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedSongs.toList()) },
                enabled = selectedSongs.isNotEmpty()
            ) {
                Text(stringResource(R.string.add_selected_songs))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
