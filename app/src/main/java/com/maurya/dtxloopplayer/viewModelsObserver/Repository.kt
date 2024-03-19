package com.maurya.dtxloopplayer.viewModelsObserver

import android.content.Context
import com.maurya.dtxloopplayer.database.FolderDataClass
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.utils.getAllFolders
import com.maurya.dtxloopplayer.utils.getAllSongs
import com.maurya.dtxloopplayer.utils.getSongsFromFolderPath
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class Repository @Inject constructor() {


    private var songs: ArrayList<MusicDataClass>? = null
    private var folders: ArrayList<FolderDataClass>? = null

    private val _songsStateFlow =
        MutableStateFlow<ModelResult<ArrayList<MusicDataClass>>>(ModelResult.Loading())

    val songsStateFlow: StateFlow<ModelResult<ArrayList<MusicDataClass>>> get() = _songsStateFlow

    private val _foldersStateFlow =
        MutableStateFlow<ModelResult<ArrayList<FolderDataClass>>>(ModelResult.Loading())

    val foldersStateFlow: StateFlow<ModelResult<ArrayList<FolderDataClass>>> get() = _foldersStateFlow


    private val _songsFromFolderStateFlow =
        MutableStateFlow<ModelResult<ArrayList<MusicDataClass>>>(ModelResult.Loading())

    val songsFromFolderStateFlow: StateFlow<ModelResult<ArrayList<MusicDataClass>>> get() = _songsFromFolderStateFlow


    private val _statusStateFlow = MutableStateFlow<ModelResult<String>>(ModelResult.Loading())
    val statusStateFlow: StateFlow<ModelResult<String>> get() = _statusStateFlow

    suspend fun getSongs(context: Context) {
        if (songs == null) {
            _songsStateFlow.emit(ModelResult.Loading())
            try {
                val fetchedSongs = getAllSongs(context)
                songs = fetchedSongs
                _songsStateFlow.emit(ModelResult.Success(fetchedSongs))
            } catch (e: Exception) {
                _songsStateFlow.emit(ModelResult.Error("Failed to fetch videos: ${e.message}"))
            }
        } else {
            _songsStateFlow.emit(ModelResult.Success(songs!!))
        }
    }

    suspend fun getFolders(context: Context) {
        if (folders == null) {
            _foldersStateFlow.emit(ModelResult.Loading())
            try {
                val fetchedFolders = getAllFolders(context)
                folders = fetchedFolders
                _foldersStateFlow.emit(ModelResult.Success(fetchedFolders))
            } catch (e: Exception) {
                _foldersStateFlow.emit(ModelResult.Error("Failed to fetch folders: ${e.message}"))
            }
        } else {
            _foldersStateFlow.emit(ModelResult.Success(folders!!))
        }
    }

    suspend fun getVideosFromFolder(context: Context, folderId: String) {
        _songsFromFolderStateFlow.emit(ModelResult.Loading())
        try {
            val videos = getSongsFromFolderPath(context, folderId)
            _songsFromFolderStateFlow.emit(ModelResult.Success(videos))
        } catch (e: Exception) {
            _songsFromFolderStateFlow.emit(ModelResult.Error("Failed to fetch videos: ${e.message}"))
        }

    }


}

