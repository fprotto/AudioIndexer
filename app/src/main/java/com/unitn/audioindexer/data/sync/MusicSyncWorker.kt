package com.unitn.audioindexer.data.sync

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.documentfile.provider.DocumentFile
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.MetadataRetriever
import androidx.media3.extractor.metadata.flac.PictureFrame
import androidx.media3.extractor.metadata.id3.ApicFrame
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import androidx.media3.extractor.metadata.vorbis.VorbisComment
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.data.network.RemoteMusicApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.core.net.toUri

class MusicSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = (context.applicationContext as AudioIndexerApplication).repository
    private val database = (context.applicationContext as AudioIndexerApplication).database

    override suspend fun doWork(): Result {
        val sourceId = inputData.getInt("source_id", -1)
        if (sourceId == -1) return Result.failure()

        val source = database.musicSourceDao().getSourceById(sourceId) ?: return Result.failure()
        
        val semaphore = Semaphore(12)
        val dbMutex = Mutex()
        
        repository.setActiveSource(sourceId)

        return try {
            when (source.type) {
                "REMOTE" -> {
                    val baseUrl = "http://${source.path}:${source.port ?: 80}/"
                    val retrofit = Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                    val api = retrofit.create(RemoteMusicApi::class.java)
                    syncRemoteFolder(api, "", sourceId, baseUrl, semaphore, dbMutex)
                }
                "LOCAL" -> {
                    val rootUri = source.path.toUri()
                    val rootDoc = DocumentFile.fromTreeUri(applicationContext, rootUri)
                    if (rootDoc != null && rootDoc.isDirectory) {
                        syncLocalFolder(rootDoc, sourceId, semaphore, dbMutex)
                    } else {
                        Log.e("MusicSyncWorker", "Invalid local source path: ${source.path}")
                        return Result.failure()
                    }
                }
                else -> return Result.failure()
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("MusicSyncWorker", "Error syncing source", e)
            Result.retry()
        }
    }

    private suspend fun syncRemoteFolder(
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
                syncRemoteFolder(api, dir.path, sourceId, baseUrl, semaphore, dbMutex)
            }
        }
    }

    private suspend fun syncLocalFolder(
        folder: DocumentFile,
        sourceId: Int,
        semaphore: Semaphore,
        dbMutex: Mutex
    ): Unit = coroutineScope {
        folder.listFiles().forEachIndexed { index, file ->
            if (file.isDirectory) {
                launch {
                    syncLocalFolder(file, sourceId, semaphore, dbMutex)
                }
            } else if (isAudioFile(file)) {
                launch {
                    processFile(file.uri.toString(), file.name ?: "Unknown", sourceId, null, semaphore, dbMutex, index)
                }
            }
        }
    }

    private fun isAudioFile(file: DocumentFile): Boolean {
        val mime = file.type ?: return false
        return mime.startsWith("audio/") || 
               file.name?.endsWith(".mp3", true) == true ||
               file.name?.endsWith(".flac", true) == true ||
               file.name?.endsWith(".m4a", true) == true ||
               file.name?.endsWith(".wav", true) == true ||
               file.name?.endsWith(".ogg", true) == true
    }

    @OptIn(UnstableApi::class)
    private suspend fun processFile(
        fileUrlOrUri: String,
        fileName: String,
        sourceId: Int,
        baseUrl: String?,
        semaphore: Semaphore,
        dbMutex: Mutex,
        fileIndex: Int
    ) {
        val fullUrlOrUri = if (baseUrl != null) {
            if (fileUrlOrUri.startsWith("http")) fileUrlOrUri else "$baseUrl${fileUrlOrUri.removePrefix("/")}"
        } else {
            fileUrlOrUri
        }
        
        val mediaItem = MediaItem.fromUri(fullUrlOrUri)
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
                val existingSong = database.songDao().getSongByPath(fullUrlOrUri, sourceId)
                
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
                    
                    if (artist.propicType == "vector") {
                        repository.resolveArtistImage(artist.id)
                    }
                }

                if (artist == null) return@withLock

                val songId = existingSong?.id?.toLong()
                    ?: repository.insertSong(
                        title = title,
                        artistId = artist.id.toLong(),
                        year = year,
                        source = if (baseUrl != null) "remote" else "local",
                        path = fullUrlOrUri,
                        artistNameOverride = if (rawArtistName != artistName) rawArtistName else null,
                        coverType = if (artworkPath != null) "uri" else "vector",
                        coverValue = artworkPath ?: "MusicNote"
                    )

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
                        if (artworkPath != null) {
                            val newAlbum = database.playlistDao().getPlaylistById(albumId.toInt())?.playlist
                            if (newAlbum != null) {
                                repository.updatePlaylist(newAlbum.copy(coverType = "uri"))
                            }
                        }
                        album = database.playlistDao().getPlaylistById(albumId.toInt())?.playlist
                    } else if (artworkPath != null && album.coverType == "vector") {
                        repository.updatePlaylist(album.copy(coverType = "uri", coverValue = artworkPath))
                    }

                    if (album != null) {
                        repository.addSongToPlaylist(album.id.toLong(), songId, trackNumber)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("MusicSyncWorker", "Error processing file: $fullUrlOrUri", e)
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
            " feat. ", " feat ", " featuring ", " ft. ", " ft ", " & ", ", "
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
