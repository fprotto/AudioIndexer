package com.unitn.audioindexer.data.repository

import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.data.components.Artist
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.data.components.Playlist
import com.unitn.audioindexer.data.components.Song
import com.unitn.audioindexer.data.components.ExportConfig
import com.unitn.audioindexer.data.components.PlaylistConfig
import com.unitn.audioindexer.data.components.SongConfig
import com.unitn.audioindexer.data.components.SourceConfig
import com.unitn.audioindexer.data.database.entities.PlaylistSongCrossRef
import kotlinx.coroutines.flow.first
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
import com.unitn.audioindexer.data.network.AudioDbApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

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

    private val audioDbApi = Retrofit.Builder()
        .baseUrl("https://www.theaudiodb.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AudioDbApi::class.java)

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

    suspend fun insertArtist(name: String, propicName: String, mbid: String? = null): Long {
        val sourceId = activeSourceId.value ?: return -1
        return artistDao.insertArtist(ArtistEntity(sourceId = sourceId, name = name, mbid = mbid, propicType = "vector", propicValue = propicName))
    }

    suspend fun insertSong(
        title: String,
        artistId: Long,
        year: Int,
        source: String,
        path: String,
        artistNameOverride: String? = null,
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
                artistNameOverride = artistNameOverride,
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

    suspend fun deletePlaylist(id: Int) {
        val playlist = playlistDao.getPlaylistById(id)?.playlist ?: return
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun renamePlaylist(id: Int, newName: String) {
        val playlist = playlistDao.getPlaylistById(id)?.playlist ?: return
        playlistDao.updatePlaylist(playlist.copy(name = newName))
    }

    suspend fun removeSongFromPlaylist(playlistId: Int, songId: Int) {
        playlistDao.deleteSongFromPlaylist(playlistId, songId)
    }

    suspend fun incrementPlayCount(songId: Int) {
        songDao.incrementPlayCount(songId)
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

    suspend fun insertPlaylist(
        name: String,
        coverName: String,
        isAlbum: Boolean = false,
        albumArtistId: Int? = null,
        releaseYear: Int? = null,
        artistNameOverride: String? = null
    ): Long {
        val sourceId = activeSourceId.value ?: return -1
        return playlistDao.insertPlaylist(
            PlaylistEntity(
                sourceId = sourceId,
                name = name,
                coverType = "vector",
                coverValue = coverName,
                isAlbum = isAlbum,
                albumArtistId = albumArtistId,
                releaseYear = releaseYear,
                artistNameOverride = artistNameOverride
            )
        )
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long, order: Int? = null) {
        val finalOrder = order ?: ((playlistDao.getMaxOrderForPlaylist(playlistId.toInt()) ?: -1) + 1)
        playlistDao.insertPlaylistSongCrossRef(
            com.unitn.audioindexer.data.database.entities.PlaylistSongCrossRef(
                playlistId = playlistId.toInt(),
                songId = songId.toInt(),
                order = finalOrder
            )
        )
    }

    suspend fun resolveArtistImage(artistId: Int) = withContext(Dispatchers.IO) {
        val artist = artistDao.getArtistById(artistId) ?: return@withContext
        
        try {
            val mbid = artist.mbid ?: return@withContext

            // Get artist thumbnail from TheAudioDB
            val adbResponse = audioDbApi.getArtistByMbid(mbid)
            val thumbUrl = adbResponse.artists?.firstOrNull()?.strArtistThumb
            
            if (thumbUrl != null) {
                val imageBytes = URL(thumbUrl).readBytes()
                val localPath = saveArtwork(imageBytes)
                if (localPath != null) {
                    artistDao.updateArtist(artist.copy(propicType = "uri", propicValue = localPath))
                }
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Error resolving artist image for ${artist.name}", e)
        }
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

    suspend fun exportActiveSourceConfiguration(): ExportConfig? {
        val sourceId = activeSourceId.value ?: return null
        val source = musicSourceDao.getSourceById(sourceId) ?: return null
        
        val playlists = playlistDao.getPlaylistsBySource(sourceId).first()
        
        val playlistConfigs = playlists.map { relation ->
            PlaylistConfig(
                name = relation.playlist.name,
                songs = relation.songs.map { songRelation ->
                    SongConfig(
                        title = songRelation.song.title,
                        artistName = songRelation.song.artistNameOverride ?: songRelation.artist.name,
                        path = songRelation.song.path
                    )
                }
            )
        }

        return ExportConfig(
            source = SourceConfig(
                type = source.type,
                path = source.path,
                port = source.port,
                name = source.name
            ),
            playlists = playlistConfigs
        )
    }

    suspend fun importConfiguration(config: ExportConfig): Result<Unit> {
        val sourceId = activeSourceId.value ?: return Result.failure(Exception("No active profile"))
        val source = musicSourceDao.getSourceById(sourceId) ?: return Result.failure(Exception("Profile not found"))

        if (source.type != config.source.type || source.path != config.source.path || source.port != config.source.port) {
            return Result.failure(Exception("Configuration source does not match active profile"))
        }

        config.playlists.forEach { playlistConfig ->
            // Find or create playlist
            var playlistEntity = playlistDao.getPlaylistByName(playlistConfig.name, sourceId, false)
            if (playlistEntity == null) {
                val newId = playlistDao.insertPlaylist(
                    PlaylistEntity(
                        sourceId = sourceId,
                        name = playlistConfig.name,
                        coverType = "vector",
                        coverValue = "FeaturedPlayList"
                    )
                )
                playlistEntity = playlistDao.getPlaylistById(newId.toInt())?.playlist
            }

            if (playlistEntity != null) {
                playlistConfig.songs.forEachIndexed { index, songConfig ->
                    val song = songDao.getSongByPath(songConfig.path, sourceId)
                    if (song != null) {
                        // Check if song already in playlist to avoid duplicates
                        val existingRefs = playlistDao.getCrossRefsForPlaylist(playlistEntity.id)
                        if (existingRefs.none { it.songId == song.id }) {
                            playlistDao.insertPlaylistSongCrossRef(
                                PlaylistSongCrossRef(
                                    playlistId = playlistEntity.id,
                                    songId = song.id,
                                    order = index
                                )
                            )
                        }
                    }
                }
            }
        }

        return Result.success(Unit)
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

    private fun SongWithArtist.toDomain(playlistOrder: Int = 0): Song {
        val iconSource = when (song.coverType) {
            "vector" -> IconSource.VectorIcon(song.coverValue)
            "uri" -> IconSource.UriIcon(song.coverValue)
            else -> IconSource.VectorIcon("MusicNote")
        }
        return Song(
            id = song.id,
            title = song.title,
            artist = artist.toDomain(),
            artistName = song.artistNameOverride ?: artist.name,
            cover = iconSource,
            path = song.path,
            releaseYear = song.releaseYear,
            playCount = song.playCount,
            playlistOrder = playlistOrder
        )
    }

    private fun PlaylistWithSongs.toDomain(): Playlist {
        val iconSource = when (playlist.coverType) {
            "vector" -> IconSource.VectorIcon(playlist.coverValue)
            "uri" -> IconSource.UriIcon(playlist.coverValue)
            else -> IconSource.VectorIcon("FeaturedPlayList")
        }
        
        val orderMap = crossRefs.associateBy({ it.songId }, { it.order })
        
        return Playlist(
            id = playlist.id,
            name = playlist.name,
            cover = iconSource,
            songs = songs.map { it.toDomain(orderMap[it.song.id] ?: 0) }
                .sortedBy { it.playlistOrder }
        )
    }

    private fun PlaylistWithSongs.toAlbumDomain(): Album {
        val iconSource = when (playlist.coverType) {
            "vector" -> IconSource.VectorIcon(playlist.coverValue)
            "uri" -> IconSource.UriIcon(playlist.coverValue)
            else -> IconSource.VectorIcon("Album")
        }
        val artist = albumArtist?.toDomain() ?: Artist(0, "Unknown", IconSource.VectorIcon("PersonOutline"))
        
        val orderMap = crossRefs.associateBy({ it.songId }, { it.order })
        val domainSongs = songs.map { it.toDomain(orderMap[it.song.id] ?: 0) }
            .sortedBy { it.playlistOrder }

        val albumYear = domainSongs.firstOrNull { it.releaseYear > 0 }?.releaseYear
            ?: playlist.releaseYear?.takeIf { it > 0 }
            ?: 0

        return Album(
            id = playlist.id,
            artist = artist,
            artistName = playlist.artistNameOverride ?: artist.name,
            name = playlist.name,
            cover = iconSource,
            releaseYear = albumYear,
            songs = domainSongs
        )
    }
}
