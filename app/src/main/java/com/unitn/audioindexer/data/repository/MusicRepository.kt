package com.unitn.audioindexer.data.repository

import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.data.components.Artist
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.data.components.Playlist
import com.unitn.audioindexer.data.components.Song
import com.unitn.audioindexer.data.database.dao.ArtistDao
import com.unitn.audioindexer.data.database.dao.MusicSourceDao
import com.unitn.audioindexer.data.database.dao.PlaylistDao
import com.unitn.audioindexer.data.database.dao.SongDao
import com.unitn.audioindexer.data.database.entities.ArtistEntity
import com.unitn.audioindexer.data.database.entities.MusicSourceEntity
import com.unitn.audioindexer.data.database.entities.PlaylistEntity
import com.unitn.audioindexer.data.database.entities.SongEntity
import com.unitn.audioindexer.data.database.relations.PlaylistWithSongs
import com.unitn.audioindexer.data.database.relations.SongWithArtist
import com.unitn.audioindexer.data.sync.RemoteSyncWorker
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import android.content.Context
import android.util.Log
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class MusicRepository(
    private val context: Context,
    private val artistDao: ArtistDao,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val musicSourceDao: MusicSourceDao
) {
    private val _activeSourceId = MutableStateFlow<Int?>(null)
    val activeSourceId: StateFlow<Int?> = _activeSourceId.asStateFlow()

    fun setActiveSource(id: Int?) {
        _activeSourceId.value = id
    }

    val allArtists: Flow<List<Artist>> = activeSourceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else artistDao.getArtistsBySource(id).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    val allSongs: Flow<List<Song>> = activeSourceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else songDao.getSongsBySource(id).map { relations ->
            relations.map { it.toDomain() }
        }
    }

    val allPlaylists: Flow<List<Playlist>> = activeSourceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else playlistDao.getPlaylistsBySource(id).map { relations ->
            relations.map { it.toDomain() }
        }
    }

    val allAlbums: Flow<List<Album>> = activeSourceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else playlistDao.getAlbumsBySource(id).map { relations ->
            relations.map { it.toAlbumDomain() }
        }
    }

    val allSources: Flow<List<MusicSourceEntity>> = musicSourceDao.getAllSources()

    suspend fun getSourceCount(): Int = musicSourceDao.getSourceCount()

    suspend fun getSourceById(id: Int): MusicSourceEntity? = musicSourceDao.getSourceById(id)

    suspend fun addSource(type: String, path: String, port: Int? = null, name: String) {
        val id = musicSourceDao.insertSource(MusicSourceEntity(type = type, path = path, port = port, name = name))
        if (_activeSourceId.value == null) {
            _activeSourceId.value = id.toInt()
        }
    }

    suspend fun deleteSource(source: MusicSourceEntity) {
        musicSourceDao.deleteSource(source)
    }

    suspend fun getArtistById(id: Int): Artist? = artistDao.getArtistById(id)?.toDomain()

    suspend fun getSongById(id: Int): Song? = songDao.getSongById(id)?.toDomain()

    suspend fun getPlaylistById(id: Int): Playlist? = playlistDao.getPlaylistById(id)?.toDomain()

    suspend fun getAlbumById(id: Int): Album? = playlistDao.getPlaylistById(id)?.toAlbumDomain()

    suspend fun insertArtist(name: String, propicName: String): Long {
        val sourceId = activeSourceId.value ?: return -1
        return artistDao.insertArtist(ArtistEntity(sourceId = sourceId, name = name, propicType = "vector", propicValue = propicName))
    }

    suspend fun insertSong(
        title: String,
        artistId: Long,
        year: Int,
        source: String,
        path: String,
        coverType: String = "vector",
        coverValue: String = "MusicNote"
    ): Long {
        val sourceId = activeSourceId.value ?: return -1
        return songDao.insertSong(
            SongEntity(
                sourceId = sourceId,
                title = title,
                artistId = artistId.toInt(),
                releaseYear = year,
                source = source,
                path = path,
                coverType = coverType,
                coverValue = coverValue
            )
        )
    }

    suspend fun updatePlaylist(playlist: PlaylistEntity) {
        playlistDao.updatePlaylist(playlist)
    }

    fun saveArtwork(artwork: ByteArray): String? {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(artwork)
            val fileName = digest.joinToString("") { "%02x".format(it) } + ".jpg"
            
            val coversDir = File(context.filesDir, "covers")
            if (!coversDir.exists()) coversDir.mkdirs()
            
            val file = File(coversDir, fileName)
            if (!file.exists()) {
                file.writeBytes(artwork)
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error saving artwork", e)
            null
        }
    }

    suspend fun insertPlaylist(name: String, coverName: String, isAlbum: Boolean = false, albumArtistId: Int? = null, releaseYear: Int? = null): Long {
        val sourceId = activeSourceId.value ?: return -1
        return playlistDao.insertPlaylist(PlaylistEntity(sourceId = sourceId, name = name, coverType = "vector", coverValue = coverName, isAlbum = isAlbum, albumArtistId = albumArtistId, releaseYear = releaseYear))
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.insertPlaylistSongCrossRef(com.unitn.audioindexer.data.database.entities.PlaylistSongCrossRef(playlistId.toInt(), songId.toInt()))
    }

    fun syncRemoteSource(sourceId: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncData = Data.Builder()
            .putInt("source_id", sourceId)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<RemoteSyncWorker>()
            .setConstraints(constraints)
            .setInputData(syncData)
            .build()

        WorkManager.getInstance(context).enqueue(syncRequest)
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
        val iconSource = when (song.coverType) {
            "vector" -> IconSource.VectorIcon(song.coverValue)
            "uri" -> IconSource.UriIcon(song.coverValue)
            else -> IconSource.VectorIcon("MusicNote")
        }
        return Song(
            id = song.id,
            title = song.title,
            artist = artist.toDomain(),
            cover = iconSource,
            path = song.path,
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
