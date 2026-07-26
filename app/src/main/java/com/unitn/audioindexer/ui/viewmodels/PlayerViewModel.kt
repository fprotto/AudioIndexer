package com.unitn.audioindexer.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import com.unitn.audioindexer.data.components.Song
import com.unitn.audioindexer.data.repository.MusicRepository
import com.unitn.audioindexer.playback.MusicController
import com.unitn.audioindexer.playback.PlaybackState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlayerUiState(
    val currentSong: Song? = null,
    val currentSongTitle: String = "Unknown",
    val currentArtist: String = "Unknown Artist",
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val positionText: String = "0:00",
    val durationText: String = "0:00",
    val isShuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val lyrics: String? = null
)

class PlayerViewModel(
    private val repository: MusicRepository,
    private val musicController: MusicController
) : ViewModel() {
    val uiState: StateFlow<PlayerUiState> = musicController.state
        .map { it.toUiState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerUiState())

    fun togglePlayPause() {
        musicController.togglePlayPause()
    }

    fun skipNext() {
        musicController.skipNext()
    }

    fun skipPrevious() {
        musicController.skipPrevious()
    }

    fun seekTo(position: Float) {
        val duration = musicController.state.value.duration
        musicController.seekTo((position * duration).toLong())
    }

    fun toggleShuffle() {
        musicController.toggleShuffle()
    }

    fun cycleRepeatMode() {
        musicController.cycleRepeatMode()
    }

    fun addSongToPlaylist(playlistId: Int, songId: Int) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId.toLong(), songId.toLong())
        }
    }

    private fun PlaybackState.toUiState(): PlayerUiState {
        Log.i("CurrentSong", currentSong.toString())
        return PlayerUiState(
            currentSong = currentSong,
            currentSongTitle = currentSong?.title ?: "Unknown",
            currentArtist = currentSong?.artistName ?: "Unknown Artist",
            isPlaying = isPlaying,
            progress = if (duration > 0) progress.toFloat() / duration else 0f,
            positionText = formatTime(progress),
            durationText = formatTime(duration),
            isShuffle = isShuffle,
            repeatMode = repeatMode,
            lyrics = currentSong?.lyrics
        )
    }

    private fun formatTime(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        return "%d:%02d".format(minutes, seconds)
    }
}
