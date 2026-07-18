package com.unitn.audioindexer

import android.app.Application
import com.unitn.audioindexer.data.database.AppDatabase
import com.unitn.audioindexer.data.repository.MusicRepository

class AudioIndexerApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { 
        MusicRepository(
            database.artistDao(),
            database.songDao(),
            database.playlistDao()
        ) 
    }
}
