package com.maurya.dtxloopplayer

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.maurya.dtxloopplayer.Activities.FavouriteActivity
import com.maurya.dtxloopplayer.Activities.PlayerActivity
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

import com.maurya.dtxloopplayer.Activities.FolderTracksActivity
import com.maurya.dtxloopplayer.Activities.PlayListActivity
import com.maurya.dtxloopplayer.Activities.SearchActivity
import com.maurya.dtxloopplayer.Fragments.SongsFragment

data class MusicData(
    val id: String,
    val title: String,
    val album: String,
    val artist: String,
    val duration: Long = 0,
    val path: String,
    val artUri: String,
    val dateModified: Long,
    var playTimestamp: Long = 0,
    var isSelected: Boolean = false
)

class PlayList() {
    lateinit var name: String
    lateinit var playList: ArrayList<MusicData>
}

class MusicPlayList() {
    var ref: ArrayList<PlayList> = ArrayList()
}

// Define a data class to store folder information

data class FolderData(
    val folderName: String,
    val folderPath: String,
    var musicFileCountInFolder: Int,
    val musicFiles: ArrayList<MusicData> // ArrayList to store music files
)

class MusicFolderScanner(private val contentResolver: ContentResolver) {

    fun getAllMusicFolders(): List<FolderData> {
        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATE_MODIFIED
        )


        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATA} ASC"

        val musicFolders = HashMap<String, FolderData>()

        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.use { cursor ->
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (cursor.moveToNext()) {
                val filePath = cursor.getString(pathColumn)
                val folderPath = filePath.substringBeforeLast("/")
                val folderName = folderPath.substringAfterLast("/")

                val folderData = musicFolders[folderPath]
                if (folderData == null) {
                    val newFolderData = FolderData(folderName, folderPath, 1, ArrayList())
                    musicFolders[folderPath] = newFolderData
                    // Create a MusicData object for this file and add it to the folder's musicFiles ArrayList
                    newFolderData.musicFiles.add(createMusicDataFromCursor(cursor, filePath))
                } else {
                    folderData.musicFileCountInFolder++
                    // Create a MusicData object for this file and add it to the folder's musicFiles ArrayList
                    folderData.musicFiles.add(createMusicDataFromCursor(cursor, filePath))
                }
            }
        }

        return musicFolders.values.toList()
    }


    private fun createMusicDataFromCursor(cursor: Cursor, filePath: String): MusicData {
        return MusicData(
            id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
            album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
            artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
            duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)),
            path = filePath,
            artUri = "",
            dateModified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED))
        )
    }
}

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
        return 0 // The folder doesn't exist or is not a directory
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



