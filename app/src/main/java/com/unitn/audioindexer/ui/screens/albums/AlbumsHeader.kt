package com.unitn.audioindexer.ui.screens.albums

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.unitn.audioindexer.R
import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.ui.toImageVector

@Composable
fun AlbumHeader(
    album: Album,
    songCount: Int,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onPlayClick: () -> Unit,
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

            // Album cover
            Box(
                modifier = Modifier
                    .size(if (isLandscape) 120.dp else 180.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                when (val cover = album.cover) {
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
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isLandscape) 2.dp else 8.dp)
            ) {
                // Album name
                Text(
                    text = album.name,
                    style = if (isLandscape) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = if (isLandscape) 2 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis
                )

                // Artist row
                Text(
                    text = album.artist.name,
                    style = if (isLandscape) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                // Metadata row: song count · release year
                Text(
                    text = buildString {
                        append(LocalResources.current.getQuantityString(
                            R.plurals.songs_count,
                            songCount,
                            songCount
                        ))
                        append("  ·  ")
                        append("${album.releaseYear}")
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Total duration row
                val totalDurationMs = album.songs.sumOf { it.duration }
                val minutes = totalDurationMs / (1000 * 60)
                val seconds = (totalDurationMs / 1000) % 60
                Text(
                    text = "%d:%02d".format(minutes, seconds),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Play button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
            }
        }
    }
}
