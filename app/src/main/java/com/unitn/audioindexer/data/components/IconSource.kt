package com.unitn.audioindexer.data.components

sealed class IconSource {
    data class VectorIcon(val name: String) : IconSource()
    data class UriIcon(val uri: String) : IconSource()
    
    // For backwards compatibility or simplified UI usage if needed
    // But we'll try to move away from direct ImageVector/ImageBitmap in the domain model
}
