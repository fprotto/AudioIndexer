package com.unitn.audioindexer.ui.screens.songs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.R
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.data.components.Song
import com.unitn.audioindexer.ui.toImageVector
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory
import com.unitn.audioindexer.ui.viewmodels.SongPropertiesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongPropertiesScreen(
    id: Int?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as AudioIndexerApplication
    val viewModel: SongPropertiesViewModel = viewModel(
        factory = MusicViewModelFactory(app.repository, app.musicController)
    )

    val song by viewModel.song.collectAsState()

    LaunchedEffect(id) {
        if (id != null) {
            viewModel.loadSong(id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.song_properties_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        song?.let { currentSong ->
            val properties = remember(currentSong) {
                getSongProperties(currentSong, context)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                item {
                    SongHeader(currentSong)
                }
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                items(properties) { property ->
                    PropertyItem(property)
                }
            }
        }
    }
}

@Composable
private fun SongHeader(song: Song) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            when (val cover = song.cover) {
                is IconSource.VectorIcon -> {
                    Icon(
                        imageVector = cover.toImageVector(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(48.dp)
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

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = song.artistName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PropertyItem(property: SongProperty) {
    ListItem(
        headlineContent = {
            Text(
                text = property.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        },
        supportingContent = {
            Text(
                text = property.value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    )
}

data class SongProperty(
    val label: String,
    val value: String
)

private fun getSongProperties(song: Song, context: android.content.Context): List<SongProperty> {
    val durationStr = if (song.duration > 0) {
        val minutes = song.duration / (1000 * 60)
        val seconds = (song.duration / 1000) % 60
        "%d:%02d".format(minutes, seconds)
    } else {
        "Unknown"
    }

    return listOf(
        SongProperty(context.getString(R.string.property_title), song.title),
        SongProperty(context.getString(R.string.property_artist), song.artistName),
        SongProperty(context.getString(R.string.property_year), song.releaseYear.toString()),
        SongProperty(context.getString(R.string.property_duration), durationStr),
        SongProperty(context.getString(R.string.property_play_count), song.playCount.toString()),
        SongProperty(context.getString(R.string.property_path), song.path)
    )
}
