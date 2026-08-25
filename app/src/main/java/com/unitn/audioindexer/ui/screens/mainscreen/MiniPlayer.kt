package com.unitn.audioindexer.ui.screens.mainscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.R
import com.unitn.audioindexer.ui.viewmodels.MiniPlayerViewModel
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory

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
