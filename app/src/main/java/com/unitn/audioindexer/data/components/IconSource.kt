package com.unitn.audioindexer.data.components

sealed class IconSource {
    data class VectorIcon(val name: String) : IconSource()
    data class UriIcon(val uri: String) : IconSource()
}
