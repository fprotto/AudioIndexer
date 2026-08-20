package com.unitn.audioindexer.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.zIndex
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
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
            (androidx.compose.ui.platform.LocalContext.current.applicationContext as AudioIndexerApplication).musicController,
            (androidx.compose.ui.platform.LocalContext.current.applicationContext as AudioIndexerApplication).settingsRepository
        )
    )
) {
    val state by viewModel.state.collectAsState()
    val queue = state.queue
    val currentSong = state.currentSong

    val listState = rememberLazyListState()
    var draggingItemIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    val queueKeys = remember(queue) {
        val occurrences = mutableMapOf<Int, Int>()
        queue.map { song ->
            val count = occurrences[song.id] ?: 0
            occurrences[song.id] = count + 1
            "${song.id}-$count"
        }
    }

    LaunchedEffect(draggingItemIndex, dragOffset) {
        val currentIdx = draggingItemIndex ?: return@LaunchedEffect
        while (draggingItemIndex != null) {
            val layoutInfo = listState.layoutInfo
            val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == currentIdx } ?: break
            val itemCenter = itemInfo.offset + itemInfo.size / 2 + dragOffset

            val viewportHeight = layoutInfo.viewportEndOffset.toFloat()
            val topThreshold = 100f
            val bottomThreshold = viewportHeight - 100f

            val scrollAmount = when {
                itemCenter < topThreshold -> -15f
                itemCenter > bottomThreshold -> 15f
                else -> 0f
            }

            if (scrollAmount != 0f) {
                listState.scrollBy(scrollAmount)
                // When we scroll, we need to adjust the dragOffset to keep the item under the finger
                // since its absolute position in the viewport changed but the finger didn't
                dragOffset += scrollAmount
                delay(10.milliseconds)
            } else {
                delay(16.milliseconds)
            }
        }
    }

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
                    .padding(8.dp)
                    .pointerInput(Unit) {
                        var offsetY = 0f
                        detectVerticalDragGestures(
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                offsetY += dragAmount
                            },
                            onDragEnd = {
                                if (offsetY > 100) {
                                    onBackClick()
                                }
                                offsetY = 0f
                            },
                            onDragCancel = {
                                offsetY = 0f
                            }
                        )
                    },
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
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(queue, key = { index, _ -> queueKeys.getOrElse(index) { index } }) { index, song ->
                    val isDragging = draggingItemIndex == index
                    val zIndex = if (isDragging) 1f else 0f

                    Column(
                        modifier = Modifier
                            .animateItem()
                            .zIndex(zIndex)
                            .graphicsLayer {
                                translationY = if (isDragging) dragOffset else 0f
                                alpha = if (isDragging) 0.8f else 1f
                            }
                    ) {
                        QueueItem(
                            song = song,
                            isCurrent = song.id == currentSong?.id,
                            onRemove = { viewModel.removeQueueItem(index) },
                            onClick = { viewModel.playQueueItem(index) },
                            dragHandleModifier = Modifier.pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { draggingItemIndex = index },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount.y

                                        val currentIdx = draggingItemIndex ?: return@detectDragGestures
                                        val itemInfo = listState.layoutInfo.visibleItemsInfo
                                            .find { it.index == currentIdx } ?: return@detectDragGestures
                                        val itemCenter = itemInfo.offset + itemInfo.size / 2 + dragOffset

                                        val targetItem = listState.layoutInfo.visibleItemsInfo
                                            .find { item ->
                                                itemCenter.toInt() in item.offset..(item.offset + item.size) &&
                                                        item.index != currentIdx
                                            }

                                        if (targetItem != null) {
                                            val scrollOffset = itemInfo.offset
                                            viewModel.moveQueueItem(currentIdx, targetItem.index)
                                            draggingItemIndex = targetItem.index
                                            dragOffset -= (targetItem.offset - scrollOffset)
                                        }
                                    },
                                    onDragEnd = {
                                        draggingItemIndex = null
                                        dragOffset = 0f
                                    },
                                    onDragCancel = {
                                        draggingItemIndex = null
                                        dragOffset = 0f
                                    }
                                )
                            }
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
}

@Composable
fun QueueItem(
    song: Song,
    isCurrent: Boolean,
    onRemove: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier
) {
    val backgroundColor = if (isCurrent) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    ListItem(
        modifier = modifier
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
                    modifier = dragHandleModifier
                        .size(24.dp)
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    )
}
