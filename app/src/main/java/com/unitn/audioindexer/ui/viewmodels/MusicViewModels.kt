package com.unitn.audioindexer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.unitn.audioindexer.data.components.Album
import com.unitn.audioindexer.data.components.Artist
import com.unitn.audioindexer.data.components.Playlist
import com.unitn.audioindexer.data.components.Song
import com.unitn.audioindexer.data.repository.MusicRepository
import com.unitn.audioindexer.playback.MusicController
import com.unitn.audioindexer.playback.PlaybackState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TracksViewModel(
    private val repository: MusicRepository,
    private val musicController: MusicController
) : ViewModel() {
    val songs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun playSong(songs: List<Song>, startIndex: Int) {
        musicController.playSongs(songs, startIndex)
    }
}

class ArtistsViewModel(
    private val repository: MusicRepository,
    private val musicController: MusicController
) : ViewModel() {
    val artists: StateFlow<List<Artist>> = repository.allArtists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun playSong(songs: List<Song>, startIndex: Int) {
        musicController.playSongs(songs, startIndex)
    }
}

class AlbumsViewModel(
    private val repository: MusicRepository,
    private val musicController: MusicController
) : ViewModel() {
    val albums: StateFlow<List<Album>> = repository.allAlbums
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun playAlbum(album: Album, shuffle: Boolean = false) {
        musicController.playSongs(album.songs, shuffle = shuffle)
    }

    fun playSong(songs: List<Song>, startIndex: Int) {
        musicController.playSongs(songs, startIndex)
    }
}

class PlaylistsViewModel(
    private val repository: MusicRepository,
    private val musicController: MusicController
) : ViewModel() {
    val playlists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun playPlaylist(playlist: Playlist, shuffle: Boolean = false) {
        musicController.playSongs(playlist.songs, shuffle = shuffle)
    }

    fun playSong(songs: List<Song>, startIndex: Int) {
        musicController.playSongs(songs, startIndex)
    }
}

class MiniPlayerViewModel(private val musicController: MusicController) : ViewModel() {
    val state: StateFlow<PlaybackState> = musicController.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaybackState())

    fun togglePlayPause() {
        musicController.togglePlayPause()
    }

    fun skipNext() {
        musicController.skipNext()
    }

    fun skipPrevious() {
        musicController.skipPrevious()
    }
}

class QueueViewModel(private val musicController: MusicController) : ViewModel() {
    val state: StateFlow<PlaybackState> = musicController.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaybackState())

    fun removeQueueItem(index: Int) {
        musicController.removeQueueItem(index)
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        musicController.moveQueueItem(fromIndex, toIndex)
    }

    fun playQueueItem(index: Int) {
        musicController.playAtIndex(index)
    }
}

class MusicViewModelFactory(
    private val repository: MusicRepository,
    private val musicController: MusicController
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(TracksViewModel::class.java) -> TracksViewModel(repository, musicController) as T
            modelClass.isAssignableFrom(ArtistsViewModel::class.java) -> ArtistsViewModel(repository, musicController) as T
            modelClass.isAssignableFrom(AlbumsViewModel::class.java) -> AlbumsViewModel(repository, musicController) as T
            modelClass.isAssignableFrom(PlaylistsViewModel::class.java) -> PlaylistsViewModel(repository, musicController) as T
            modelClass.isAssignableFrom(SetupViewModel::class.java) -> SetupViewModel(repository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(repository) as T
            modelClass.isAssignableFrom(PlayerViewModel::class.java) ->
                PlayerViewModel(musicController) as T
            modelClass.isAssignableFrom(MiniPlayerViewModel::class.java) -> MiniPlayerViewModel(musicController) as T
            modelClass.isAssignableFrom(QueueViewModel::class.java) -> QueueViewModel(musicController) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
