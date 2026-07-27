package com.unitn.audioindexer.ui.screens.player

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.R
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.data.components.Song
import com.unitn.audioindexer.ui.components.AddToPlaylistDialog
import com.unitn.audioindexer.ui.components.CreatePlaylistDialog
import com.unitn.audioindexer.ui.toImageVector
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory
import com.unitn.audioindexer.ui.viewmodels.PlayerUiState
import com.unitn.audioindexer.ui.viewmodels.PlayerViewModel
import com.unitn.audioindexer.ui.viewmodels.PlaylistsViewModel

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = viewModel(
        factory = MusicViewModelFactory(
            (androidx.compose.ui.platform.LocalContext.current.applicationContext as AudioIndexerApplication).repository,
            (androidx.compose.ui.platform.LocalContext.current.applicationContext as AudioIndexerApplication).musicController,
            (androidx.compose.ui.platform.LocalContext.current.applicationContext as AudioIndexerApplication).settingsRepository
        )
    ),
    onBackClick: () -> Unit = {},
    onQueueClick: () -> Unit = {},
    onLyricsClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as AudioIndexerApplication
    val playlistsViewModel: PlaylistsViewModel = viewModel(factory = MusicViewModelFactory(app.repository, app.musicController, app.settingsRepository))
    val playlists by playlistsViewModel.playlists.collectAsState()

    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showAddToPlaylistDialog) {
        val currentSong = uiState.currentSong
        if (currentSong != null) {
            AddToPlaylistDialog(
                playlists = playlists,
                onDismissRequest = { showAddToPlaylistDialog = false },
                onPlaylistSelected = { playlist ->
                    viewModel.addSongToPlaylist(playlist.id, currentSong.id)
                    showAddToPlaylistDialog = false
                },
                onCreateNewPlaylist = {
                    showCreateDialog = true
                }
            )
        }
    }

    if (showCreateDialog) {
        CreatePlaylistDialog(
            onDismissRequest = { showCreateDialog = false },
            onConfirm = { name ->
                playlistsViewModel.createPlaylist(name)
                showCreateDialog = false
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Album Art
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp)
                ) {
                    AlbumArt(
                        song = uiState.currentSong,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                }

                // Right side: Controls
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PlayerTopBar(
                        onBackClick = onBackClick,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    SongInfo(
                        uiState = uiState,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PlayerProgress(
                        uiState = uiState,
                        onSeek = viewModel::seekTo,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    PlaybackControls(
                        uiState = uiState,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    BottomActions(
                        onQueueClick = onQueueClick,
                        onLyricsClick = onLyricsClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlayerTopBar(
                    onBackClick = onBackClick
                )

                Spacer(modifier = Modifier.height(32.dp))

                AlbumArt(
                    song = uiState.currentSong,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                SongInfo(uiState = uiState)

                Spacer(modifier = Modifier.height(32.dp))

                PlayerProgress(uiState = uiState, onSeek = viewModel::seekTo)

                Spacer(modifier = Modifier.weight(1f))

                PlaybackControls(uiState = uiState, viewModel = viewModel)

                Spacer(modifier = Modifier.weight(1f))

                BottomActions(
                    onQueueClick = onQueueClick,
                    onLyricsClick = onLyricsClick
                )
            }
        }
    }
}

@Composable
private fun PlayerTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.minimize)
            )
        }
        Text(
            text = stringResource(R.string.now_playing_uppercase),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun AlbumArt(
    song: Song?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (song != null) {
            when (val cover = song.cover) {
                is IconSource.VectorIcon -> Icon(
                    imageVector = cover.toImageVector(),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                is IconSource.UriIcon -> AsyncImage(
                    model = cover.uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun SongInfo(
    uiState: PlayerUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = uiState.currentSongTitle,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = uiState.currentArtist,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlayerProgress(
    uiState: PlayerUiState,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Slider(
            value = uiState.progress,
            onValueChange = onSeek,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = uiState.positionText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = uiState.durationText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun PlaybackControls(
    uiState: PlayerUiState,
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = viewModel::toggleShuffle,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = if (uiState.isShuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                Icons.Default.Shuffle,
                contentDescription = stringResource(R.string.shuffle)
            )
        }

        IconButton(onClick = viewModel::skipPrevious, modifier = Modifier.size(48.dp)) {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = stringResource(R.string.previous),
                modifier = Modifier.size(36.dp)
            )
        }

        FilledIconButton(
            onClick = viewModel::togglePlayPause,
            modifier = Modifier.size(72.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(
                if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (uiState.isPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                modifier = Modifier.size(40.dp)
            )
        }

        IconButton(onClick = viewModel::skipNext, modifier = Modifier.size(48.dp)) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = stringResource(R.string.next),
                modifier = Modifier.size(36.dp)
            )
        }

        IconButton(
            onClick = viewModel::cycleRepeatMode,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = if (uiState.repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                when (uiState.repeatMode) {
                    Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                    else -> Icons.Default.Repeat
                },
                contentDescription = stringResource(R.string.repeat)
            )
        }
    }
}

@Composable
private fun BottomActions(
    onQueueClick: () -> Unit,
    onLyricsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(onClick = onQueueClick) {
            Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.up_next_uppercase))
        }
        
        Spacer(modifier = Modifier.width(16.dp))

        TextButton(onClick = onLyricsClick) {
            Icon(Icons.Default.Description, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.lyrics).uppercase())
        }
    }
}
