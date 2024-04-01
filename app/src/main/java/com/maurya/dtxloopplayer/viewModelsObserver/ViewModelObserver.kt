package com.maurya.dtxloopplayer.viewModelsObserver

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.utils.SharedPreferenceHelper
import com.maurya.dtxloopplayer.utils.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelObserver @Inject constructor(private val repository: Repository) :
    ViewModel() {

    val songsStateFLow get() = repository.songsStateFlow

    val foldersStateFLow get() = repository.foldersStateFlow
    val songFromFoldersStateFLow get() = repository.songsFromFolderStateFlow
    val songFromPlayListStateFLow get() = repository.songsFromPlayListStateFlow


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
                repository.getMusicFromFolder(context, folderId)
            } catch (e: Exception) {
                showToast(context, e.message.toString())
            }
        }

    }


    fun fetchSongsFromPlayList(
        context: Context,
        playListName: String,
        sharedPreferenceHelper: SharedPreferenceHelper
    ) {
        viewModelScope.launch {
            try {
                repository.getMusicFromPlayList(playListName, sharedPreferenceHelper)
            } catch (e: Exception) {
                showToast(context, e.message.toString())
            }
        }
    }


    //for song name, artist, seekbar
    private val _songInfo = MutableLiveData<MusicDataClass>()
    val songInfo: LiveData<MusicDataClass> get() = _songInfo

    fun setMusicData(musicData: MusicDataClass) {
        _songInfo.value = musicData
    }
}