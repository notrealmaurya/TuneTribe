package com.maurya.dtxloopplayer.dataEntity

import android.content.ContentResolver
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.maurya.dtxloopplayer.activities.FavouriteActivity
import com.maurya.dtxloopplayer.activities.PlayerActivity
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

import com.maurya.dtxloopplayer.activities.FolderTracksActivity
import com.maurya.dtxloopplayer.activities.PlayListActivity
import com.maurya.dtxloopplayer.activities.SearchActivity
import com.maurya.dtxloopplayer.fragments.SongsFragment

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
                    newFolderData.musicFiles.add(createMusicDataFromCursor(cursor, filePath))
                } else {
                    folderData.musicFileCountInFolder++
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




