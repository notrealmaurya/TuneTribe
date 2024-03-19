package com.maurya.flexivid.viewModelsObserver

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maurya.dtxloopplayer.viewModelsObserver.Repository
import com.maurya.flexivid.util.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelObserver @Inject constructor(private val repository: Repository) :
    ViewModel(), DefaultLifecycleObserver {

    val videosStateFLow get() = repository.videosStateFlow

    val foldersStateFLow get() = repository.foldersStateFlow
    val videoFromFoldersStateFLow get() = repository.videosFromFolderStateFlow


    fun fetchVideos(context: Context) {
        viewModelScope.launch {
            try {
                repository.getVideos(context)
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

    fun fetchVideosFromFolder(context: Context, folderId: String) {
        viewModelScope.launch {
            try {
                repository.getVideosFromFolder(context, folderId)
            } catch (e: Exception) {
                showToast(context, e.message.toString())
            }
        }

    }


}