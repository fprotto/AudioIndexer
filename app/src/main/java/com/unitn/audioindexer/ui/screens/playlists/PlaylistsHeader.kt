package com.unitn.audioindexer.ui.screens.playlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AddToQueue
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.unitn.audioindexer.R
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.data.components.Playlist
import com.unitn.audioindexer.ui.toImageVector

@Composable
fun PlaylistHeader(
    playlist: Playlist,
    songCount: Int,
    onNavigateBack: () -> Unit,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit,
    modifier: Modifier = Modifier,
    onUpdateCoverClick: () -> Unit = {},
    onRemoveCoverClick: () -> Unit = {},
    onAddSongsClick: () -> Unit = {},
    onAddToQueue: () -> Unit = {},
    onRenameClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    sortOrder: PlaylistSongSortOrder = PlaylistSongSortOrder.CUSTOM,
    onSortOrderChange: (PlaylistSongSortOrder) -> Unit = {},
    isLandscape: Boolean = false
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .statusBarsPadding()
    ) {
        // Back button pinned at the top
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = stringResource(R.string.navigate_back),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = if (isLandscape) 40.dp else 56.dp,
                    bottom = if (isLandscape) 16.dp else 24.dp
                )
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (isLandscape) Arrangement.SpaceEvenly else Arrangement.spacedBy(16.dp)
        ) {
            if (isLandscape) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Playlist cover
            Box(
                modifier = Modifier
                    .size(if (isLandscape) 120.dp else 180.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onUpdateCoverClick() },
                contentAlignment = Alignment.Center
            ) {
                when (val cover = playlist.cover) {
                    is IconSource.VectorIcon -> Icon(
                        imageVector = cover.toImageVector(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(if (isLandscape) 56.dp else 80.dp)
                    )
                    is IconSource.UriIcon -> {
                        AsyncImage(
                            model = cover.uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isLandscape) 4.dp else 8.dp)
            ) {
                // Playlist name
                Text(
                    text = playlist.name,
                    style = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = if (isLandscape) 2 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis
                )

                // Metadata row: song count
                Text(
                    text = buildString {
                        append(LocalResources.current.getQuantityString(
                            R.plurals.songs_count,
                            songCount,
                            songCount
                        ))
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action buttons row
            var showMenu by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play button
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = if (isLandscape) ButtonDefaults.TextButtonContentPadding else ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.play),
                        modifier = Modifier.size(if (isLandscape) 18.dp else ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(if (isLandscape) 4.dp else ButtonDefaults.IconSpacing))
                    Text(
                        text = stringResource(R.string.play),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = if (isLandscape) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyLarge
                    )
                }

                // Shuffle button
                FilledTonalButton(
                    onClick = onShuffleClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = if (isLandscape) ButtonDefaults.TextButtonContentPadding else ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = stringResource(R.string.shuffle_play),
                        modifier = Modifier.size(if (isLandscape) 18.dp else ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(if (isLandscape) 4.dp else ButtonDefaults.IconSpacing))
                    Text(
                        text = stringResource(R.string.shuffle_play),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = if (isLandscape) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyLarge
                    )
                }

                // More options
                var sortExpanded by remember { mutableStateOf(false) }

                Box {
                    IconButton(onClick = { sortExpanded = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.sort)
                        )
                    }
                    DropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.custom_order)) },
                            onClick = { onSortOrderChange(PlaylistSongSortOrder.CUSTOM); sortExpanded = false },
                            trailingIcon = {
                                if (sortOrder == PlaylistSongSortOrder.CUSTOM) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.title)) },
                            onClick = { onSortOrderChange(PlaylistSongSortOrder.TITLE); sortExpanded = false },
                            trailingIcon = {
                                if (sortOrder == PlaylistSongSortOrder.TITLE) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.artist)) },
                            onClick = { onSortOrderChange(PlaylistSongSortOrder.ARTIST); sortExpanded = false },
                            trailingIcon = {
                                if (sortOrder == PlaylistSongSortOrder.ARTIST) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.release_year)) },
                            onClick = { onSortOrderChange(PlaylistSongSortOrder.YEAR); sortExpanded = false },
                            trailingIcon = {
                                if (sortOrder == PlaylistSongSortOrder.YEAR) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more_options)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                                    contentDescription = stringResource(R.string.menu_add_to_playlist),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text(stringResource(R.string.menu_add_to_playlist)) },
                            onClick = {
                                showMenu = false
                                onAddSongsClick()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AddToQueue,
                                    contentDescription = stringResource(R.string.menu_add_to_queue),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text(stringResource(R.string.menu_add_to_queue)) },
                            onClick = {
                                showMenu = false
                                onAddToQueue()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DriveFileRenameOutline,
                                    contentDescription = stringResource(R.string.menu_rename),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text(stringResource(R.string.menu_rename)) },
                            onClick = {
                                showMenu = false
                                onRenameClick()
                            }
                        )
                        if (playlist.cover is IconSource.UriIcon) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.HideImage,
                                        contentDescription = stringResource(R.string.delete_playlist_image),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                text = { Text(stringResource(R.string.delete_playlist_image)) },
                                onClick = {
                                    showMenu = false
                                    onRemoveCoverClick()
                                }
                            )
                        }
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.menu_delete_playlist),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            text = {
                                Text(
                                    stringResource(R.string.menu_delete_playlist),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }
        }
    }
}
