package com.maurya.dtxloopplayer.viewModelsObserver

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maurya.dtxloopplayer.utils.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelObserver @Inject constructor(private val repository: Repository) :
    ViewModel(), DefaultLifecycleObserver {

    val songsStateFLow get() = repository.songsStateFlow

    val foldersStateFLow get() = repository.foldersStateFlow
    val songFromFoldersStateFLow get() = repository.songsFromFolderStateFlow


    fun fetchSongs(context: Context) {
        viewModelScope.launch {
            try {
                repository.getSongs(context)
            } catch (e: Exception) {
                showToast(context, e.message.toString())
            }
        }
    }

    fun fetchFolders(context: Context) {
        viewModelScope.launch {
            try {
                repository.getFolders(context)
            } catch (e: Exception) {
                showToast(context, e.message.toString())
            }
        }
    }

    fun fetchSongsFromFolder(context: Context, folderId: String) {
        viewModelScope.launch {
            try {
                repository.getVideosFromFolder(context, folderId)
            } catch (e: Exception) {
                showToast(context, e.message.toString())
            }
        }

    }


}