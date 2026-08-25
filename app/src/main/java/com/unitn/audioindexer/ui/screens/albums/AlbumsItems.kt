package com.unitn.audioindexer.ui.screens.albums

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.ui.screens.Screen
import com.unitn.audioindexer.ui.toImageVector

@Composable
fun AlbumItem(
    album: Album,
    navController: NavController,
    modifier: Modifier = Modifier,
    showAsCard: Boolean = false,
    onAddToQueue: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    if (showAsCard) {
        Card(
            onClick = { navController.navigate(Screen.Album.createRoute(album.id)) },
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    when (val cover = album.cover) {
                        is IconSource.VectorIcon -> Icon(
                            imageVector = cover.toImageVector(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(56.dp)
                        )
                        is IconSource.UriIcon -> {
                            AsyncImage(
                                model = cover.uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = album.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = album.artist.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    AlbumMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        onMoreOptionsClick = { showMenu = true },
                        onAddToQueue = onAddToQueue,
                        onAddToPlaylist = onAddToPlaylist
                    )
                }
            }
        }
    } else {
        ListItem(
            headlineContent = {
                Text(
                    text = album.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(
                    text = album.artist.name,
                    style = MaterialTheme.typography.bodySmall,
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
                    when (val cover = album.cover) {
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
                AlbumMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    onMoreOptionsClick = { showMenu = true },
                    onAddToQueue = onAddToQueue,
                    onAddToPlaylist = onAddToPlaylist
                )
            },
            modifier = Modifier.clickable(onClick = { navController.navigate(Screen.Album.createRoute(album.id)) })
        )
    }
}
