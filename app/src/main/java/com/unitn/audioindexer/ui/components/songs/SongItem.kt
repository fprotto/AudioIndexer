package com.unitn.audioindexer.ui.components.songs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToQueue
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistAddCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.unitn.audioindexer.R
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.data.components.Song
import com.unitn.audioindexer.ui.toImageVector

@Composable
fun SongCard(
    song: Song,
    onClick: () -> Unit = {},
    onAddToQueue: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {},
    onPropertiesClick: () -> Unit = {},
    onRemoveFromPlaylist: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    isPlaying: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = song.artistName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                when (val cover = song.cover) {
                    is IconSource.VectorIcon -> {
                        Icon(
                            imageVector = cover.toImageVector(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    is IconSource.UriIcon -> {
                        AsyncImage(
                            model = cover.uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (song.duration > 0) {
                    val minutes = song.duration / (1000 * 60)
                    val seconds = (song.duration / 1000) % 60
                    Text(
                        text = "%d:%02d".format(minutes, seconds),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more_options),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
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
                                    imageVector = Icons.Default.PlaylistAddCircle,
                                    contentDescription = stringResource(R.string.menu_add_to_playlist),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text(stringResource(R.string.menu_add_to_playlist)) },
                            onClick = {
                                showMenu = false
                                onAddToPlaylist()
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = stringResource(R.string.menu_properties),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            text = { Text(stringResource(R.string.menu_properties)) },
                            onClick = {
                                showMenu = false
                                onPropertiesClick()
                            }
                        )
                        if (onRemoveFromPlaylist != null) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.menu_remove_from_playlist),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                text = {
                                    Text(
                                        stringResource(R.string.menu_remove_from_playlist),
                                        color = MaterialTheme.colorScheme.error
                                    )
                               },
                                onClick = {
                                    showMenu = false
                                    onRemoveFromPlaylist()
                                }
                            )
                        } else if (onDelete != null) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.menu_delete),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                text = {
                                    Text(
                                        stringResource(R.string.menu_delete),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
