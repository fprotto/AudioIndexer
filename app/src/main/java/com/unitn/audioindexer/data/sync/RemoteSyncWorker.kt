package com.unitn.audioindexer.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.data.network.RemoteMusicApi
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.MetadataRetriever
import androidx.media3.extractor.metadata.id3.ApicFrame
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import androidx.media3.extractor.metadata.vorbis.VorbisComment
import androidx.media3.extractor.metadata.flac.PictureFrame
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.annotation.OptIn
import kotlinx.coroutines.suspendCancellableCoroutine

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

        response.files.forEachIndexed { index, file ->
            launch {
                processFile(file.url, file.name, sourceId, baseUrl, semaphore, dbMutex, index)
            }
        }

        for (dir in response.directories) {
            launch {
                syncFolder(api, dir.path, sourceId, baseUrl, semaphore, dbMutex)
            }
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun processFile(
        fileUrl: String,
        fileName: String,
        sourceId: Int,
        baseUrl: String,
        semaphore: Semaphore,
        dbMutex: Mutex,
        fileIndex: Int
    ) {
        val fullUrl = if (fileUrl.startsWith("http")) fileUrl else "$baseUrl${fileUrl.removePrefix("/")}"
        
        val mediaItem = MediaItem.fromUri(fullUrl)
        try {
            val trackGroups = semaphore.withPermit {
                MetadataRetriever.retrieveMetadata(applicationContext, mediaItem).await()
            }
            
            var year = 0
            var artwork: ByteArray? = null
            var mbid: String? = null
            val metadataBuilder = MediaMetadata.Builder()

            for (i in 0 until trackGroups.length) {
                val trackGroup = trackGroups.get(i)
                for (j in 0 until trackGroup.length) {
                    val format = trackGroup.getFormat(j)
                    format.metadata?.let { metadata ->
                        for (k in 0 until metadata.length()) {
                            val entry = metadata.get(k)
                            entry.populateMediaMetadata(metadataBuilder)
                            
                            when (entry) {
                                is TextInformationFrame -> {
                                    val entryYear = entry.values.firstOrNull()?.let { parseYear(it) } ?: 0
                                    if (entryYear != 0) {
                                        if (entry.id in listOf("TDOR", "TORY")) {
                                            year = entryYear // Priority: Original Year
                                        } else if (year == 0 && entry.id in listOf("TDRC", "TYER", "TDRL")) {
                                            year = entryYear // Fallback: Release Year
                                        }
                                    }
                                    
                                    if (entry.id == "TXXX" && entry.description?.contains("MusicBrainz Artist Id", ignoreCase = true) == true) {
                                        mbid = entry.values.firstOrNull()?.split(";")?.firstOrNull()?.trim()
                                    }
                                }
                                is VorbisComment -> {
                                    val entryYear = parseYear(entry.value)
                                    if (entryYear != 0) {
                                        if (entry.key.equals("ORIGINALYEAR", ignoreCase = true) || entry.key.equals("ORIGINALDATE", ignoreCase = true)) {
                                            year = entryYear // Priority: Original Year
                                        } else if (year == 0 && (entry.key.equals("DATE", ignoreCase = true) || entry.key.equals("YEAR", ignoreCase = true))) {
                                            year = entryYear // Fallback: Release Year
                                        }
                                    }
                                    
                                    if (entry.key.equals("MUSICBRAINZ_ARTISTID", ignoreCase = true)) {
                                        mbid = entry.value.split(";").firstOrNull()?.trim()
                                    }
                                }
                                is ApicFrame -> if (artwork == null) artwork = entry.pictureData
                                is PictureFrame -> if (artwork == null) artwork = entry.pictureData
                            }
                        }
                    }
                }
            }
            
            val mediaMetadata = metadataBuilder.build()
            val title = mediaMetadata.title?.toString() ?: fileName
            val rawArtistName = (mediaMetadata.artist ?: mediaMetadata.albumArtist ?: "Unknown Artist").toString()
            val artistName = normalizeArtistName(rawArtistName)
            
            val albumName = mediaMetadata.albumTitle?.toString()
            if (year == 0) year = mediaMetadata.releaseYear ?: 0
            
            val trackNumber = mediaMetadata.trackNumber ?: (fileIndex + 1)
            if (artwork == null) artwork = mediaMetadata.artworkData
            
            val artworkPath = artwork?.let { repository.saveArtwork(it) }

            dbMutex.withLock {
                // Check if song already exists (within the lock to be safe)
                val existingSong = database.songDao().getSongByPath(fullUrl, sourceId)
                
                // 1. Handle Artist
                var artist = database.artistDao().getArtistByName(artistName, sourceId)
                if (artist == null) {
                    val artistId = repository.insertArtist(artistName, "PersonOutline", mbid)
                    artist = database.artistDao().getArtistById(artistId.toInt())
                    if (artist != null) {
                        repository.resolveArtistImage(artist.id)
                    }
                } else {
                    var needsUpdate = false
                    var updatedArtist = artist
                    
                    if (mbid != null && artist.mbid == null) {
                        updatedArtist = updatedArtist.copy(mbid = mbid)
                        needsUpdate = true
                    }
                    
                    if (needsUpdate) {
                        database.artistDao().updateArtist(updatedArtist)
                        artist = updatedArtist
                    }
                    
                    // Always try to resolve image if it's still a vector icon
                    if (artist.propicType == "vector") {
                        repository.resolveArtistImage(artist.id)
                    }
                }

                if (artist == null) return@withLock

                // 2. Handle Song
                val songId = existingSong?.id?.toLong()
                    ?: repository.insertSong(
                        title = title,
                        artistId = artist.id.toLong(),
                        year = year,
                        source = "remote",
                        path = fullUrl,
                        artistNameOverride = if (rawArtistName != artistName) rawArtistName else null,
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
                            releaseYear = year,
                            artistNameOverride = if (rawArtistName != artistName) rawArtistName else null
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
                        repository.addSongToPlaylist(album.id.toLong(), songId, trackNumber)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("RemoteSyncWorker", "Error processing file: $fullUrl", e)
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun <T> ListenableFuture<T>.await(): T = suspendCancellableCoroutine { cont ->
        addListener({
            try {
                cont.resume(get())
            } catch (e: Exception) {
                cont.resumeWithException(e)
            }
        }, MoreExecutors.directExecutor())
        cont.invokeOnCancellation {
            cancel(true)
        }
    }

    private fun parseYear(dateString: String): Int {
        return Regex("\\d{4}").find(dateString)?.value?.toIntOrNull() ?: 0
    }

    private fun normalizeArtistName(name: String): String {
        val featuringSeparators = listOf(
            " feat. ", " feat ", " featuring ", " ft. ", " ft "
        )
        
        var normalized = name
        featuringSeparators.forEach { separator ->
            val index = normalized.indexOf(separator, ignoreCase = true)
            if (index != -1) {
                normalized = normalized.substring(0, index)
            }
        }
        return normalized.trim()
    }
}
