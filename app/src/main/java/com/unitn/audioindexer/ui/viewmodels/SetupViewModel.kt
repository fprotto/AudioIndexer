package com.unitn.audioindexer.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.unitn.audioindexer.data.repository.MusicRepository

class SetupViewModel(private val repository: MusicRepository) : ViewModel() {

    suspend fun addLocalSource(uri: String): Int {
        return repository.addSource(type = "LOCAL", path = uri, name = "Local Library")
    }

    suspend fun addRemoteSource(name: String, ip: String, port: Int): Int {
        return repository.addSource(type = "REMOTE", path = ip, port = port, name = name)
    }
}
