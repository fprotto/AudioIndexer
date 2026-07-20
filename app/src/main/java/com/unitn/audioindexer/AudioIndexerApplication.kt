package com.unitn.audioindexer

import android.app.Application
import com.unitn.audioindexer.data.database.AppDatabase
import com.unitn.audioindexer.data.repository.MusicRepository
import com.unitn.audioindexer.playback.MusicController

class AudioIndexerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { 
        MusicRepository(
            this,
            database.artistDao(),
            database.songDao(),
            database.playlistDao(),
            database.musicSourceDao()
        ) 
    }
    val musicController by lazy { MusicController(this, repository) }
}
