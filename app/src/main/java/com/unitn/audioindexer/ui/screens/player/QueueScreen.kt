package com.unitn.audioindexer.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.R
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.data.components.Song
import com.unitn.audioindexer.ui.toImageVector
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory
import com.unitn.audioindexer.ui.viewmodels.QueueViewModel

@Composable
fun QueueScreen(
    onBackClick: () -> Unit,
    viewModel: QueueViewModel = viewModel(
        factory = MusicViewModelFactory(
            (androidx.compose.ui.platform.LocalContext.current.applicationContext as AudioIndexerApplication).repository,
            (androidx.compose.ui.platform.LocalContext.current.applicationContext as AudioIndexerApplication).musicController
        )
    )
) {
    val state by viewModel.state.collectAsState()
    val queue = state.queue
    val currentSong = state.currentSong

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
                Text(
                    text = stringResource(R.string.up_next_uppercase),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(queue) { index, song ->
                    QueueItem(
                        song = song,
                        isCurrent = song.id == currentSong?.id,
                        onRemove = { viewModel.removeQueueItem(index) },
                        onClick = { viewModel.playQueueItem(index) }
                    )
                    if (index < queue.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QueueItem(
    song: Song,
    isCurrent: Boolean,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    val backgroundColor = if (isCurrent) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = song.artistName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                when (val cover = song.cover) {
                    is IconSource.VectorIcon -> Icon(
                        imageVector = cover.toImageVector(),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    is IconSource.UriIcon -> AsyncImage(
                        model = cover.uri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.menu_delete),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    )
}
