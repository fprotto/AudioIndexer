package com.unitn.audioindexer.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.PersonOutline
import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.data.components.AlbumUiState
import com.unitn.audioindexer.data.components.Artist
import com.unitn.audioindexer.data.components.ArtistUiState
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.data.components.Playlist
import com.unitn.audioindexer.data.components.PlaylistUiState
import com.unitn.audioindexer.data.components.Song

fun sampleArtist() : Artist {
    return Artist(
        id = 1,
        name = "Artist",
        propic = IconSource.VectorIcon(Icons.Default.PersonOutline)
    )
}

fun sampleSongs() : List<Song> {
    val artist = sampleArtist()

    return listOf(
        Song(
            id = 1,
            title = "Title",
            artist = artist,
            releaseYear = 2023
        ),
        Song(
            id = 1,
            title = "Title",
            artist = artist,
            releaseYear = 2023
        ),
        Song(
            id = 1,
            title = "Title",
            artist = artist,
            releaseYear = 2023
        ),
        Song(
            id = 1,
            title = "Title",
            artist = artist,
            releaseYear = 2023
        ),
        Song(
            id = 1,
            title = "Title",
            artist = artist,
            releaseYear = 2023
        )
    )
}

fun sampleArtistsState() = ArtistUiState(
    artists = listOf(
        sampleArtist(),
        sampleArtist(),
        sampleArtist(),
        sampleArtist(),
        sampleArtist(),
    )
)

fun sampleAlbumsState() = AlbumUiState(
    albums = listOf(
        Album(
            id = 1,
            artist = sampleArtist(),
            name = "Album 1",
            cover = IconSource.VectorIcon(Icons.Default.Album),
            releaseYear = 2012,
            songs = sampleSongs()
        )
    )
)

fun samplePlaylistsState() = PlaylistUiState(
    playlists = listOf(
        Playlist(
            id = 1,
            name = "Playlist 1",
            cover = IconSource.VectorIcon(Icons.Default.FeaturedPlayList),
            songs = sampleSongs()
        )
    )
)
