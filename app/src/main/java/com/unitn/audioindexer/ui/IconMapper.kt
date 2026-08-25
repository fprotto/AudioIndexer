package com.unitn.audioindexer.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FeaturedPlayList
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.ui.graphics.vector.ImageVector
import com.unitn.audioindexer.data.components.IconSource

fun IconSource.toImageVector(): ImageVector {
    return when (this) {
        is IconSource.VectorIcon -> {
            when (name) {
                "PersonOutline" -> Icons.Default.PersonOutline
                "Album" -> Icons.Default.Album
                "FeaturedPlayList" -> Icons.AutoMirrored.Filled.FeaturedPlayList
                else -> Icons.Default.MusicNote
            }
        }
        is IconSource.UriIcon -> Icons.Default.MusicNote // Fallback
    }
}
