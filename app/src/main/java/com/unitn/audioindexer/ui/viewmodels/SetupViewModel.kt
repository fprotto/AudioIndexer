package com.unitn.audioindexer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unitn.audioindexer.data.repository.MusicRepository
import kotlinx.coroutines.launch

class SetupViewModel(private val repository: MusicRepository) : ViewModel() {

    fun addLocalSource(uri: String) {
        viewModelScope.launch {
            repository.addSource(type = "LOCAL", path = uri, name = "Local Library")
        }
    }

    fun addRemoteSource(name: String, ip: String, port: Int) {
        viewModelScope.launch {
            repository.addSource(type = "REMOTE", path = ip, port = port, name = name)
        }
    }
}
