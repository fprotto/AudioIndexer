package com.unitn.audioindexer.data.sync

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.data.database.entities.PlaylistSongCrossRef
import com.unitn.audioindexer.data.network.RemoteMusicApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RemoteSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = (context.applicationContext as AudioIndexerApplication).repository
    private val database = (context.applicationContext as AudioIndexerApplication).database

    override suspend fun doWork(): Result {
        val sourceId = inputData.getInt("source_id", -1)
        if (sourceId == -1) return Result.failure()

        val source = database.musicSourceDao().getSourceById(sourceId) ?: return Result.failure()
        if (source.type != "REMOTE") return Result.failure()

        val baseUrl = "http://${source.path}:${source.port ?: 80}/"
        
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(RemoteMusicApi::class.java)

        val semaphore = Semaphore(12)
        val dbMutex = Mutex()
        
        repository.setActiveSource(sourceId)

        return try {
            syncFolder(api, "", sourceId, baseUrl, semaphore, dbMutex)
            Result.success()
        } catch (e: Exception) {
            Log.e("RemoteSyncWorker", "Error syncing remote source", e)
            Result.retry()
        }
    }

    private suspend fun syncFolder(
        api: RemoteMusicApi,
        path: String,
        sourceId: Int,
        baseUrl: String,
        semaphore: Semaphore,
        dbMutex: Mutex
    ): Unit = coroutineScope {
        val response = if (path.isEmpty()) api.browseRoot() else api.browsePath(path)

        for (file in response.files) {
            launch {
                processFile(file.url, file.name, sourceId, baseUrl, semaphore, dbMutex)
            }
        }

        for (dir in response.directories) {
            launch {
                syncFolder(api, dir.path, sourceId, baseUrl, semaphore, dbMutex)
            }
        }
    }

    private suspend fun processFile(
        fileUrl: String,
        fileName: String,
        sourceId: Int,
        baseUrl: String,
        semaphore: Semaphore,
        dbMutex: Mutex
    ) {
        val fullUrl = if (fileUrl.startsWith("http")) fileUrl else "$baseUrl${fileUrl.removePrefix("/")}"
        
        // Check if song already exists
        val existingSong = database.songDao().getSongByPath(fullUrl, sourceId)
        if (existingSong != null) return

        val retriever = MediaMetadataRetriever()
        try {
            semaphore.withPermit {
                retriever.setDataSource(fullUrl, HashMap<String, String>())
            }
            
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: fileName
            val artistName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) ?: "Unknown Artist"
            val albumName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val yearStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
            val year = yearStr?.toIntOrNull() ?: 0
            
            val artwork = retriever.embeddedPicture
            val artworkPath = artwork?.let { repository.saveArtwork(it) }

            dbMutex.withLock {
                // 1. Handle Artist
                var artist = database.artistDao().getArtistByName(artistName, sourceId)
                if (artist == null) {
                    val artistId = repository.insertArtist(artistName, "PersonOutline")
                    artist = database.artistDao().getArtistById(artistId.toInt())
                }

                if (artist == null) return@withLock

                // 2. Handle Song
                val songId = repository.insertSong(
                    title = title,
                    artistId = artist.id.toLong(),
                    year = year,
                    source = "remote",
                    path = fullUrl,
                    coverType = if (artworkPath != null) "uri" else "vector",
                    coverValue = artworkPath ?: "MusicNote"
                )

                // 3. Handle Album
                if (albumName != null) {
                    var album = database.playlistDao().getPlaylistByName(albumName, sourceId, isAlbum = true)
                    if (album == null) {
                        val albumId = repository.insertPlaylist(
                            name = albumName,
                            coverName = artworkPath ?: "Album",
                            isAlbum = true,
                            albumArtistId = artist.id,
                            releaseYear = year
                        )
                        // If we have artwork, set coverType to uri
                        if (artworkPath != null) {
                            val newAlbum = database.playlistDao().getPlaylistById(albumId.toInt())?.playlist
                            if (newAlbum != null) {
                                repository.updatePlaylist(newAlbum.copy(coverType = "uri"))
                            }
                        }
                        album = database.playlistDao().getPlaylistById(albumId.toInt())?.playlist
                    } else if (artworkPath != null && album.coverType == "vector") {
                        // Update album cover if it was default and we found artwork
                        repository.updatePlaylist(album.copy(coverType = "uri", coverValue = artworkPath))
                    }

                    if (album != null) {
                        database.playlistDao().insertPlaylistSongCrossRef(
                            PlaylistSongCrossRef(album.id, songId.toInt())
                        )
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("RemoteSyncWorker", "Error processing file: $fullUrl", e)
        } finally {
            retriever.release()
        }
    }
}
