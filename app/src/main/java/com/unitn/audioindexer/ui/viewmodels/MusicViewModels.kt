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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TracksViewModel(
    private val repository: MusicRepository,
    private val musicController: MusicController
) : ViewModel() {
    val songs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun playSong(songs: List<Song>, startIndex: Int) {
        musicController.playSongs(songs, startIndex)
    }

    fun addToQueue(song: Song) {
        musicController.addSongsToQueue(listOf(song))
    }

    fun addSongToPlaylist(playlistId: Int, songId: Int) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId.toLong(), songId.toLong())
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            repository.deleteSong(song.id)
        }
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

    fun addToQueue(song: Song) {
        musicController.addSongsToQueue(listOf(song))
    }

    fun addAlbumToQueue(album: Album) {
        musicController.addSongsToQueue(album.songs)
    }

    fun addSongToPlaylist(playlistId: Int, songId: Int) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId.toLong(), songId.toLong())
        }
    }

    fun addSongsToPlaylist(playlistId: Int, songs: List<Song>) {
        viewModelScope.launch {
            songs.forEach {
                repository.addSongToPlaylist(playlistId.toLong(), it.id.toLong())
            }
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            repository.deleteSong(song.id)
        }
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

    fun addToQueue(song: Song) {
        musicController.addSongsToQueue(listOf(song))
    }

    fun addAlbumToQueue(album: Album) {
        musicController.addSongsToQueue(album.songs)
    }

    fun addSongToPlaylist(playlistId: Int, songId: Int) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId.toLong(), songId.toLong())
        }
    }

    fun addSongsToPlaylist(playlistId: Int, songs: List<Song>) {
        viewModelScope.launch {
            songs.forEach {
                repository.addSongToPlaylist(playlistId.toLong(), it.id.toLong())
            }
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            repository.deleteSong(song.id)
        }
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

    fun addToQueue(song: Song) {
        musicController.addSongsToQueue(listOf(song))
    }

    fun addPlaylistToQueue(playlist: Playlist) {
        musicController.addSongsToQueue(playlist.songs)
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.insertPlaylist(name, "FeaturedPlayList")
        }
    }

    fun deletePlaylist(playlistId: Int) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun renamePlaylist(playlistId: Int, newName: String) {
        viewModelScope.launch {
            repository.renamePlaylist(playlistId, newName)
        }
    }

    fun addSongToPlaylist(playlistId: Int, songId: Int) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId.toLong(), songId.toLong())
        }
    }

    fun addSongsToPlaylist(playlistId: Int, songs: List<Song>) {
        viewModelScope.launch {
            songs.forEach {
                repository.addSongToPlaylist(playlistId.toLong(), it.id.toLong())
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: Int, songId: Int) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            repository.deleteSong(song.id)
        }
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

class SongPropertiesViewModel(
    private val repository: MusicRepository
) : ViewModel() {
    private val _song = MutableStateFlow<Song?>(null)
    val song: StateFlow<Song?> = _song.asStateFlow()

    fun loadSong(id: Int) {
        viewModelScope.launch {
            _song.value = repository.getSongById(id)
        }
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
            modelClass.isAssignableFrom(PlayerViewModel::class.java) -> PlayerViewModel(repository, musicController) as T
            modelClass.isAssignableFrom(MiniPlayerViewModel::class.java) -> MiniPlayerViewModel(musicController) as T
            modelClass.isAssignableFrom(QueueViewModel::class.java) -> QueueViewModel(musicController) as T
            modelClass.isAssignableFrom(SongPropertiesViewModel::class.java) -> SongPropertiesViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
