package com.unitn.audioindexer.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.unitn.audioindexer.data.components.IconSource
import com.unitn.audioindexer.data.components.Song
import com.unitn.audioindexer.data.repository.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import kotlin.time.Duration.Companion.milliseconds

data class PlaybackState(
    val currentSong: Song? = null,
    val queue: List<Song> = emptyList(),
    val isPlaying: Boolean = false,
    val progress: Long = 0,
    val duration: Long = 0,
    val isShuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF
)

class MusicController(
    private val context: Context,
    private val repository: MusicRepository
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val controller: MediaController?
        get() = if (controllerFuture?.isDone == true) controllerFuture?.get() else null

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null
    private var lastSourceId: Int? = null

    init {
        initialize()
        scope.launch {
            repository.activeSourceId.collect { id ->
                if (lastSourceId != null && lastSourceId != id) {
                    resetPlayer()
                }
                lastSourceId = id
            }
        }
    }

    fun initialize() {
        if (controllerFuture != null) return

        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            updateState()
            controller?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _state.value = _state.value.copy(isPlaying = isPlaying)
                    if (isPlaying) startProgressUpdate() else stopProgressUpdate()
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    updateCurrentSong(mediaItem)
                    if (mediaItem != null) {
                        scope.launch {
                            try {
                                val songId = mediaItem.mediaId.toInt()
                                repository.incrementPlayCount(songId)
                            } catch (e: Exception) {
                                // Ignore if mediaId is not an Int
                            }
                        }
                    }
                }

                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int
                ) {
                    updateProgress()
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    _state.value = _state.value.copy(isShuffle = shuffleModeEnabled)
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    _state.value = _state.value.copy(repeatMode = repeatMode)
                }

                override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                    updateQueue()
                }
            })
        }, MoreExecutors.directExecutor())
    }

    private fun resetPlayer() {
        val player = controller ?: return
        player.stop()
        player.clearMediaItems()
        _state.value = PlaybackState()
    }

    fun destroyPlayer() {
        val player = controller ?: return
        player.stop()
        player.clearMediaItems()
        player.release()
        controllerFuture?.cancel(true)
        controllerFuture = null
    }

    private fun updateState() {
        val player = controller ?: return
        _state.value = PlaybackState(
            currentSong = player.currentMediaItem?.toSong(),
            queue = getQueueFromPlayer(player),
            isPlaying = player.isPlaying,
            progress = player.currentPosition,
            duration = player.duration,
            isShuffle = player.shuffleModeEnabled,
            repeatMode = player.repeatMode
        )
        if (player.isPlaying) startProgressUpdate()
    }

    private fun updateCurrentSong(mediaItem: MediaItem?) {
        val player = controller
        _state.value = _state.value.copy(
            currentSong = mediaItem?.toSong(),
            duration = player?.duration ?: 0,
            queue = player?.let { getQueueFromPlayer(it) } ?: _state.value.queue
        )
    }

    private fun updateQueue() {
        val player = controller ?: return
        _state.value = _state.value.copy(
            queue = getQueueFromPlayer(player)
        )
    }

    private fun getQueueFromPlayer(player: Player): List<Song> {
        val queue = mutableListOf<Song>()
        for (i in 0 until player.mediaItemCount) {
            player.getMediaItemAt(i).toSong().let { queue.add(it) }
        }
        return queue
    }

    private fun updateProgress() {
        val player = controller ?: return
        _state.value = _state.value.copy(
            progress = player.currentPosition,
            duration = player.duration
        )
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                updateProgress()
                delay(1000.milliseconds)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0, shuffle: Boolean = false) {
        val player = controller ?: return
        val mediaItems =
            if (shuffle)
                songs.shuffled().map { it.toMediaItem() }
            else
                songs.map { it.toMediaItem() }

        player.setMediaItems(mediaItems)
        player.shuffleModeEnabled = shuffle
        player.prepare()
        
        player.seekTo(startIndex, 0)
        player.play()
    }

    fun togglePlayPause() {
        val player = controller ?: return
        if (player.isPlaying) player.pause() else player.play()
    }

    fun skipNext() {
        controller?.seekToNext()

        val player = controller ?: return
        if (!player.isPlaying) player.play()
    }

    fun skipPrevious() {
        controller?.seekToPrevious()

        val player = controller ?: return
        if (!player.isPlaying) player.play()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    fun playAtIndex(index: Int) {
        controller?.seekTo(index, 0)
    }

    fun toggleShuffle() {
        val player = controller ?: return
        player.shuffleModeEnabled = !player.shuffleModeEnabled
    }

    fun cycleRepeatMode() {
        val player = controller ?: return
        player.repeatMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun removeQueueItem(index: Int) {
        controller?.removeMediaItem(index)
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        controller?.moveMediaItem(fromIndex, toIndex)
    }

    fun addSongsToQueue(songs: List<Song>) {
        val player = controller ?: return
        val mediaItems = songs.map { it.toMediaItem() }
        player.addMediaItems(mediaItems)
    }

    private fun Song.toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artistName)
            .setExtras(android.os.Bundle().apply {
                putInt("id", id)
                putString("coverType", when(cover) {
                    is IconSource.VectorIcon -> "vector"
                    is IconSource.UriIcon -> "uri"
                })
                putString("coverValue", when(val c = cover) {
                    is IconSource.VectorIcon -> c.name
                    is IconSource.UriIcon -> c.uri
                })
                putInt("releaseYear", releaseYear)
                putString("lyrics", lyrics)
            })
            .build()

        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(path.toUri())
            .setMediaMetadata(metadata)
            .build()
    }

    private fun MediaItem.toSong(): Song {
        val metadata = mediaMetadata
        val extras = metadata.extras
        val coverType = extras?.getString("coverType")
        val coverValue = extras?.getString("coverValue") ?: ""
        val cover = if (coverType == "uri") IconSource.UriIcon(coverValue) 
                    else IconSource.VectorIcon(coverValue.ifEmpty { "MusicNote" })
        val artist = com.unitn.audioindexer.data.components.Artist(0, metadata.artist?.toString() ?: "Unknown", IconSource.VectorIcon("PersonOutline"))
        val artistName = artist.name
        val lyrics = extras?.getString("lyrics")

        return Song(
            id = mediaId.toInt(),
            title = metadata.title?.toString() ?: "Unknown",
            artistName = artistName,
            artist = artist,
            cover = cover,
            path = requestMetadata.mediaUri?.toString() ?: "",
            releaseYear = extras?.getInt("releaseYear") ?: 0,
            lyrics = lyrics
        )
    }
}
