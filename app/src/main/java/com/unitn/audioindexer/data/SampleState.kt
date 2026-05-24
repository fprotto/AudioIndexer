package com.unitn.audioindexer.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.PersonOutline
import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.data.components.AlbumUiState
import com.unitn.audioindexer.data.components.Artist
import com.unitn.audioindexer.data.components.ArtistUiState
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.data.components.Playlist
import com.unitn.audioindexer.data.components.PlaylistUiState
import com.unitn.audioindexer.data.components.Song

private val artist1 = Artist(
    id = 1,
    name = "The Melodic Echoes",
    propic = IconSource.VectorIcon(Icons.Default.PersonOutline)
)

private val artist2 = Artist(
    id = 2,
    name = "Luna Star",
    propic = IconSource.VectorIcon(Icons.Default.PersonOutline)
)

private val artist3 = Artist(
    id = 3,
    name = "Jazz Quartet",
    propic = IconSource.VectorIcon(Icons.Default.PersonOutline)
)

private val artist4 = Artist(
    id = 4,
    name = "Cyber Rhythm",
    propic = IconSource.VectorIcon(Icons.Default.PersonOutline)
)

private val songsArtist1 = listOf(
    Song(id = 101, title = "Whispering Winds", artist = artist1, releaseYear = 2021),
    Song(id = 102, title = "Silent Echoes", artist = artist1, releaseYear = 2021),
    Song(id = 103, title = "Mountain Path", artist = artist1, releaseYear = 2021),
    Song(id = 104, title = "Ocean Breeze", artist = artist1, releaseYear = 2021),
    Song(id = 105, title = "Forest Light", artist = artist1, releaseYear = 2021)
)

private val songsArtist2 = listOf(
    Song(id = 201, title = "Neon Nights", artist = artist2, releaseYear = 2022),
    Song(id = 202, title = "Starlight Dance", artist = artist2, releaseYear = 2022),
    Song(id = 203, title = "Midnight Ride", artist = artist2, releaseYear = 2022),
    Song(id = 204, title = "Electric Heart", artist = artist2, releaseYear = 2022),
    Song(id = 205, title = "Glow in the Dark", artist = artist2, releaseYear = 2022)
)

private val songsArtist3 = listOf(
    Song(id = 301, title = "Blue Moods", artist = artist3, releaseYear = 2019),
    Song(id = 302, title = "Smooth Journey", artist = artist3, releaseYear = 2019),
    Song(id = 303, title = "Evening Jazz", artist = artist3, releaseYear = 2019),
    Song(id = 304, title = "Saxophone Solo", artist = artist3, releaseYear = 2019),
    Song(id = 305, title = "Rhythm Section", artist = artist3, releaseYear = 2019)
)

private val songsArtist4 = listOf(
    Song(id = 401, title = "Digital Soul", artist = artist4, releaseYear = 2023),
    Song(id = 402, title = "Binary Beat", artist = artist4, releaseYear = 2023),
    Song(id = 403, title = "Virtual Reality", artist = artist4, releaseYear = 2023),
    Song(id = 404, title = "Cyber Pulse", artist = artist4, releaseYear = 2023),
    Song(id = 405, title = "Future Sound", artist = artist4, releaseYear = 2023)
)

fun sampleSongs() : List<Song> = songsArtist1 + songsArtist2 + songsArtist3 + songsArtist4

private val album1 = Album(
    id = 1,
    artist = artist1,
    name = "Whispers of the Wind",
    cover = IconSource.VectorIcon(Icons.Default.Album),
    releaseYear = 2021,
    songs = songsArtist1
)

private val album2 = Album(
    id = 2,
    artist = artist2,
    name = "Neon Nights",
    cover = IconSource.VectorIcon(Icons.Default.Album),
    releaseYear = 2022,
    songs = songsArtist2
)

private val album3 = Album(
    id = 3,
    artist = artist3,
    name = "Blue Moods",
    cover = IconSource.VectorIcon(Icons.Default.Album),
    releaseYear = 2019,
    songs = songsArtist3
)

private val album4 = Album(
    id = 4,
    artist = artist4,
    name = "Digital Soul",
    cover = IconSource.VectorIcon(Icons.Default.Album), releaseYear = 2023,
    songs = songsArtist4
)

fun sampleArtistsState() = ArtistUiState(
    artists = listOf(artist1, artist2, artist3, artist4)
)

fun sampleAlbumsState() = AlbumUiState(
    albums = listOf(album1, album2, album3, album4)
)

fun samplePlaylistsState() = PlaylistUiState(
    playlists = listOf(
        Playlist(
            id = 1,
            name = "Morning Vibes",
            cover = IconSource.VectorIcon(Icons.AutoMirrored.Filled.FeaturedPlayList),
            songs = listOf(songsArtist1[0], songsArtist2[1], songsArtist3[2])
        ),
        Playlist(
            id = 2,
            name = "Late Night Study",
            cover = IconSource.VectorIcon(Icons.AutoMirrored.Filled.FeaturedPlayList),
            songs = songsArtist3 + songsArtist4.take(2)
        ),
        Playlist(
            id = 3,
            name = "Workout Mix",
            cover = IconSource.VectorIcon(Icons.AutoMirrored.Filled.FeaturedPlayList),
            songs = songsArtist2 + songsArtist4
        ),
        Playlist(
            id = 4,
            name = "Relaxing Piano",
            cover = IconSource.VectorIcon(Icons.AutoMirrored.Filled.FeaturedPlayList),
            songs = songsArtist1.take(3) + songsArtist3.take(2)
        )
    )
)
