package com.unitn.audioindexer.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.unitn.audioindexer.data.database.dao.ArtistDao
import com.unitn.audioindexer.data.database.dao.MusicSourceDao
import com.unitn.audioindexer.data.database.dao.PlaylistDao
import com.unitn.audioindexer.data.database.dao.SongDao
import com.unitn.audioindexer.data.database.entities.ArtistEntity
import com.unitn.audioindexer.data.database.entities.MusicSourceEntity
import com.unitn.audioindexer.data.database.entities.PlaylistEntity
import com.unitn.audioindexer.data.database.entities.PlaylistSongCrossRef
import com.unitn.audioindexer.data.database.entities.SongEntity

@Database(
    entities = [
        ArtistEntity::class,
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        MusicSourceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun artistDao(): ArtistDao
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun musicSourceDao(): MusicSourceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "audio_indexer_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
