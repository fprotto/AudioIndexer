package com.unitn.audioindexer.data.network

data class BrowseResponse(
    val path: String,
    val directories: List<DirectoryItem>,
    val files: List<FileItem>
)

data class DirectoryItem(
    val name: String,
    val path: String
)

data class FileItem(
    val name: String,
    val size: Long,
    val url: String
)
