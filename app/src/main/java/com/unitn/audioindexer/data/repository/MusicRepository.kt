package com.unitn.audioindexer.data.repository

import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.data.components.Artist
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.data.components.Playlist
import com.unitn.audioindexer.data.components.Song
import com.unitn.audioindexer.data.database.dao.ArtistDao
import com.unitn.audioindexer.data.database.dao.PlaylistDao
import com.unitn.audioindexer.data.database.dao.SongDao
import com.unitn.audioindexer.data.database.entities.ArtistEntity
import com.unitn.audioindexer.data.database.entities.PlaylistEntity
import com.unitn.audioindexer.data.database.entities.SongEntity
import com.unitn.audioindexer.data.database.relations.PlaylistWithSongs
import com.unitn.audioindexer.data.database.relations.SongWithArtist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepository(
    private val artistDao: ArtistDao,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao
) {
    val allArtists: Flow<List<Artist>> = artistDao.getAllArtists().map { entities ->
        entities.map { it.toDomain() }
    }

    val allSongs: Flow<List<Song>> = songDao.getAllSongsWithArtist().map { relations ->
        relations.map { it.toDomain() }
    }

    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists().map { relations ->
        relations.map { it.toDomain() }
    }

    val allAlbums: Flow<List<Album>> = playlistDao.getAllAlbums().map { relations ->
        relations.map { it.toAlbumDomain() }
    }

    suspend fun getArtistById(id: Int): Artist? = artistDao.getArtistById(id)?.toDomain()

    suspend fun getSongById(id: Int): Song? = songDao.getSongById(id)?.toDomain()

    suspend fun getPlaylistById(id: Int): Playlist? = playlistDao.getPlaylistById(id)?.toDomain()

    suspend fun getAlbumById(id: Int): Album? = playlistDao.getPlaylistById(id)?.toAlbumDomain()

    suspend fun seedIfEmpty() {
        if (artistDao.getArtistById(1) != null) return

        val a1Id = insertArtist("The Melodic Echoes", "PersonOutline")
        val a2Id = insertArtist("Luna Star", "PersonOutline")

        insertSong("Whispering Winds", a1Id, 2021, "local", "/sdcard/music/wind.mp3")
        insertSong("Silent Echoes", a1Id, 2021, "local", "/sdcard/music/echoes.mp3")
        insertSong("Neon Nights", a2Id, 2022, "remote", "http://server.com/neon.mp3")

        val p1Id = insertPlaylist("Morning Vibes", "FeaturedPlayList")
        addSongToPlaylist(p1Id, 1)
        addSongToPlaylist(p1Id, 2)

        insertPlaylist("Whispers of the Wind", "Album", isAlbum = true, albumArtistId = a1Id.toInt(), releaseYear = 2021)
    }

    suspend fun insertArtist(name: String, propicName: String): Long {
        return artistDao.insertArtist(ArtistEntity(name = name, propicType = "vector", propicValue = propicName))
    }

    suspend fun insertSong(title: String, artistId: Long, year: Int, source: String, path: String) {
        songDao.insertSong(SongEntity(title = title, artistId = artistId.toInt(), releaseYear = year, source = source, path = path))
    }

    suspend fun insertPlaylist(name: String, coverName: String, isAlbum: Boolean = false, albumArtistId: Int? = null, releaseYear: Int? = null): Long {
        return playlistDao.insertPlaylist(PlaylistEntity(name = name, coverType = "vector", coverValue = coverName, isAlbum = isAlbum, albumArtistId = albumArtistId, releaseYear = releaseYear))
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.insertPlaylistSongCrossRef(com.unitn.audioindexer.data.database.entities.PlaylistSongCrossRef(playlistId.toInt(), songId.toInt()))
    }

    // Mappers
    private fun ArtistEntity.toDomain(): Artist {
        val iconSource = when (propicType) {
            "vector" -> IconSource.VectorIcon(propicValue)
            "uri" -> IconSource.UriIcon(propicValue)
            else -> IconSource.VectorIcon("PersonOutline")
        }
        return Artist(id, name, iconSource)
    }

    private fun SongWithArtist.toDomain(): Song {
        return Song(
            id = song.id,
            title = song.title,
            artist = artist.toDomain(),
            releaseYear = song.releaseYear,
            playCount = song.playCount
        )
    }

    private fun PlaylistWithSongs.toDomain(): Playlist {
        val iconSource = when (playlist.coverType) {
            "vector" -> IconSource.VectorIcon(playlist.coverValue)
            "uri" -> IconSource.UriIcon(playlist.coverValue)
            else -> IconSource.VectorIcon("FeaturedPlayList")
        }
        return Playlist(
            id = playlist.id,
            name = playlist.name,
            cover = iconSource,
            songs = songs.map { it.toDomain() }
        )
    }

    private fun PlaylistWithSongs.toAlbumDomain(): Album {
        val iconSource = when (playlist.coverType) {
            "vector" -> IconSource.VectorIcon(playlist.coverValue)
            "uri" -> IconSource.UriIcon(playlist.coverValue)
            else -> IconSource.VectorIcon("Album")
        }
        val artist = albumArtist?.toDomain() ?: Artist(0, "Unknown", IconSource.VectorIcon("PersonOutline"))
        
        return Album(
            id = playlist.id,
            artist = artist,
            name = playlist.name,
            cover = iconSource,
            releaseYear = playlist.releaseYear ?: 0,
            songs = songs.map { it.toDomain() }
        )
    }
}
