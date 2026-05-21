package com.unitn.audioindexer.data.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector

sealed class IconSource {
    data class VectorIcon(val imageVector: ImageVector) : IconSource()
    data class BitmapIcon(val bitmap: ImageBitmap) : IconSource()
}
