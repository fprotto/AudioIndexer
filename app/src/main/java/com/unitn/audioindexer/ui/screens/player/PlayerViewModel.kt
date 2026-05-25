package com.unitn.audioindexer.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.media3.common.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerUiState(
    val currentSongTitle: String = "Unknown",
    val currentArtist: String = "Unknown Artist",
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val positionText: String = "0:00",
    val durationText: String = "0:00",
    val isShuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
)

class PlayerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    // In a real implementation, we would inject a PlayerController or MediaController here
    // for now we just manage local state

    fun togglePlayPause() {
        _uiState.value = _uiState.value.copy(isPlaying = !_uiState.value.isPlaying)
    }

    fun skipNext() {
        // Logic to skip next
    }

    fun skipPrevious() {
        // Logic to skip previous
    }

    fun seekTo(position: Float) {
        _uiState.value = _uiState.value.copy(progress = position)
    }

    fun toggleShuffle() {
        _uiState.value = _uiState.value.copy(isShuffle = !_uiState.value.isShuffle)
    }

    fun cycleRepeatMode() {
        val nextMode = when (_uiState.value.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _uiState.value = _uiState.value.copy(repeatMode = nextMode)
    }
}
