package com.unitn.audioindexer.ui.screens.albums

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToQueue
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlaylistAddCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.unitn.audioindexer.R

@Composable
fun AlbumMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onMoreOptionsClick: () -> Unit,
    onAddToQueue: () -> Unit = {},
    onAddToPlaylist: () -> Unit = {}
) {
    Box {
        IconButton(onClick = onMoreOptionsClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.more_options),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest
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
                    onDismissRequest()
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
                    onDismissRequest()
                    onAddToPlaylist()
                }
            )
        }
    }
}
