package com.maurya.dtxloopplayer.utils

import android.media.MediaMetadataRetriever
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.maurya.dtxloopplayer.activities.FavouriteActivity
import com.maurya.dtxloopplayer.activities.FolderTracksActivity
import com.maurya.dtxloopplayer.activities.PlayListActivity
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.activities.SearchActivity
import com.maurya.dtxloopplayer.database.MusicData
import com.maurya.dtxloopplayer.fragments.SongsFragment
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess


fun formatDuration(duration: Long): String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            TimeUnit.MINUTES.toSeconds(minutes)

    return String.format("%02d:%02d", minutes, seconds)
}

fun getMusicArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increment: Boolean) {
    if (!PlayerActivity.repeat) {
        if (increment) {
            if (PlayerActivity.musicListPlayerActivity.size - 1 == PlayerActivity.musicPosition) {
                PlayerActivity.musicPosition = 0
            } else {
                ++PlayerActivity.musicPosition
            }

        } else {
            if (0 == PlayerActivity.musicPosition) {
                PlayerActivity.musicPosition = PlayerActivity.musicListPlayerActivity.size - 1
            } else {
                --PlayerActivity.musicPosition
            }
        }
    }
}

fun exitApplication() {
    if (PlayerActivity.musicService != null) {
        PlayerActivity.musicService!!.audioManager.abandonAudioFocus(PlayerActivity.musicService)
        PlayerActivity.musicService!!.stopForeground(true)
        PlayerActivity.musicService!!.mediaPlayer!!.release()
        PlayerActivity.musicService = null
    }
    exitProcess(1)
}

fun favouriteChecker(id: String): Int {
    PlayerActivity.isFavourite = false
    FavouriteActivity.favouriteSongs.forEachIndexed { index, musicData ->
        if (id == musicData.id) {
            PlayerActivity.isFavourite = true
            return index
        }
    }
    return -1
}

fun checkPlayListData(playList: ArrayList<MusicData>): ArrayList<MusicData> {
    playList.forEachIndexed { index, musicData ->
        val file = File(musicData.path)
        if (!file.exists()) {
            playList.removeAt(index)
        }
    }
    return playList
}

fun updateTextViewWithItemCount(adapter: RecyclerView.Adapter<*>, textView: TextView) {
    val itemCount = adapter.itemCount

    val itemCountText = if (itemCount == 1 || itemCount == 0) {
        "$itemCount song"
    } else {
        "$itemCount songs"
    }
    textView.text = itemCountText
    adapter.notifyDataSetChanged()
}

fun updateTextViewWithFolderCount(adapter: RecyclerView.Adapter<*>, textView: TextView) {
    val itemCount = adapter.itemCount

    val itemCountText = if (itemCount == 1 || itemCount == 0) {
        "$itemCount folder"
    } else {
        "$itemCount folders"
    }
    textView.text = itemCountText
}

fun countMusicFilesInFolder(folderPath: String): Int {
    val folder = File(folderPath)
    if (!folder.exists() || !folder.isDirectory) {
        return 0
    }

    val musicFileExtensions =
        listOf(".mp3", ".wav", ".ogg", ".flac") // Add more extensions if needed

    var musicFileCount = 0

    val folderFiles = folder.listFiles()
    if (folderFiles != null) {
        for (file in folderFiles) {
            if (file.isFile) {
                val fileName = file.name.toLowerCase()
                if (musicFileExtensions.any { fileName.endsWith(it) }) {
                    musicFileCount++
                }
            }
        }
    }

    return musicFileCount
}

fun notifyAdapterSongTextPosition() {
    if (SongsFragment.isInitialized) {
        SongsFragment.musicAdapter.notifyDataSetChanged()
    }
    if (SearchActivity.isInitialized) {
        SearchActivity.musicAdapter.notifyDataSetChanged()
    }
    if (PlayListActivity.isInitialized) {
        PlayListActivity.musicAdapter.notifyDataSetChanged()
    }
    if (FavouriteActivity.isInitialized) {
        FavouriteActivity.favouriteAdapter.notifyDataSetChanged()
    }
    if (FolderTracksActivity.isInitialized) {
        FolderTracksActivity.musicAdapter.notifyDataSetChanged()
    }
}


