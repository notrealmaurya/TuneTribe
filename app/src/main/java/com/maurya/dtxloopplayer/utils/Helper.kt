package com.maurya.dtxloopplayer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore.Audio.Media.*
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.maurya.dtxloopplayer.activities.FavouriteActivity
import com.maurya.dtxloopplayer.activities.FolderTracksActivity
import com.maurya.dtxloopplayer.activities.PlayListActivity
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.activities.SearchActivity
import com.maurya.dtxloopplayer.adapter.AdapterMusic
import com.maurya.dtxloopplayer.database.FolderDataClass
import com.maurya.dtxloopplayer.database.MusicDataClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.ln
import kotlin.math.pow
import kotlin.system.exitProcess


//for retrieving songs
suspend fun getAllSongs(
    context: Context,
    folderId: String = "",
    isFolder: Boolean = false
): ArrayList<MusicDataClass> =
    withContext(Dispatchers.IO) {
        val tempList = ArrayList<MusicDataClass>()

        Log.d("tempItemClass", folderId)

        val projection = arrayOf(
            _ID,
            DISPLAY_NAME,
            ARTIST,
            BUCKET_DISPLAY_NAME,
            DURATION,
            DATA,
            SIZE,
            ALBUM_ID,
            DATE_MODIFIED
        )

        val selection: String?
        val selectionArgs: Array<String>?

//        if (isFolder) {
//            selection =
//                "$DATA LIKE ? AND $DURATION >= ? AND $IS_MUSIC != 0 AND ($MIME_TYPE != ? AND $MIME_TYPE != ? AND $MIME_TYPE != ?)"
//            selectionArgs = arrayOf("$folderId%", "30000", "audio/mp3", "audio/mp4", "audio/x-m4a")
//        } else {
//            selection =
//                "$DURATION >= ? AND ($MIME_TYPE != ? AND $MIME_TYPE != ? AND $MIME_TYPE != ?)"
//            selectionArgs = arrayOf("30000", "audio/mp3", "audio/mp4", "audio/x-m4a")
//        }

        if (isFolder) {
            selection =
                "$DATA LIKE ? AND $DURATION >= ? AND $IS_MUSIC != 0"
            selectionArgs = arrayOf("$folderId%", "30000")
        } else {
            selection =
                "$DURATION >= ? AND $IS_MUSIC != 0"
            selectionArgs = arrayOf("30000")
        }



        val cursor = context.contentResolver.query(
            EXTERNAL_CONTENT_URI, projection, selection, selectionArgs,
            "DATE_ADDED DESC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val idCursor = it.getString(it.getColumnIndexOrThrow(_ID))
                val musicNameCursor =
                    it.getString(it.getColumnIndexOrThrow(DISPLAY_NAME))
                val musicAlbumCursor =
                    it.getString(it.getColumnIndexOrThrow(ARTIST))
                val folderNameCursor =
                    it.getString(it.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME))
                val durationCursor =
                    it.getLong(it.getColumnIndexOrThrow(DURATION))
                val data = it.getString(it.getColumnIndexOrThrow(DATA))
                val musicSizeCursor =
                    it.getString(it.getColumnIndexOrThrow(SIZE))
                val dateModified =
                    it.getLong(it.getColumnIndexOrThrow(DATE_MODIFIED))
                val albumIdC = it.getLong(it.getColumnIndexOrThrow(ALBUM_ID)).toString()

                val uri = Uri.parse("content://media/external/audio/albumart")
                val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()

                val fileCursor = File(data)

                if (fileCursor.exists()) {
                    val musicData = MusicDataClass(
                        idCursor,
                        musicNameCursor,
                        folderNameCursor,
                        durationCursor,
                        musicSizeCursor,
                        musicAlbumCursor,
                        data,
                        artUriC,
                        dateModified
                    )
                    tempList.add(musicData)
                } else {

                }

            }

        }

        return@withContext tempList
    }


//for fetching folders
suspend fun getAllFolders(
    context: Context
): ArrayList<FolderDataClass> =
    withContext(Dispatchers.IO) {

        val tempFolderList = ArrayList<String>()
        val folderList = ArrayList<FolderDataClass>()

        val selection =
            "$DURATION >= ? AND $IS_MUSIC != 0"
        val selectionArgs = arrayOf("30000")

        val projection = arrayOf(
           BUCKET_DISPLAY_NAME, BUCKET_ID,DATA
        )
        val cursor = context.contentResolver.query(
            EXTERNAL_CONTENT_URI, projection, selection, selectionArgs,
            "DATE_ADDED DESC"
        )
        cursor?.use {
            while (it.moveToNext()) {
                val folderNameCursor = it.getString(it.getColumnIndexOrThrow(BUCKET_DISPLAY_NAME))
                val folderIdCursor = it.getString(it.getColumnIndexOrThrow(BUCKET_ID))
                val data = it.getString(it.getColumnIndexOrThrow(DATA))
                val folderPath = data.substringBeforeLast("/")

                if (!tempFolderList.contains(folderNameCursor) && !folderNameCursor.contains("Internal Storage")) {
                    tempFolderList.add(folderNameCursor)
                    folderList.add(FolderDataClass(folderIdCursor, folderNameCursor, folderPath, 0))
                }
            }
        }
        return@withContext folderList
    }


//sorting Video List
fun sortMusicList(
    sortBy: String,
    videoList: ArrayList<MusicDataClass>,
    adapterMusic: AdapterMusic
) {
    when (sortBy) {
        "DATE_ADDED ASC" -> videoList.sortBy { it.dateModified }
        "DATE_ADDED DESC" -> videoList.sortByDescending { it.dateModified }
        "SIZE ASC" -> videoList.sortBy { it.durationText }
        "SIZE DESC" -> videoList.sortByDescending { it.durationText }
        "DISPLAY_NAME ASC" -> videoList.sortBy { it.musicName.lowercase() }
        "DISPLAY_NAME DESC" -> videoList.sortByDescending { it.musicName.lowercase() }
        else -> {
            videoList.sortByDescending { it.dateModified }
        }
    }
    adapterMusic.notifyDataSetChanged()
}


//counting files in folder
fun countMusicFilesInFolder(context: Context, folderPath: String): Int {
    val projection = arrayOf(DATA)
    val selection = "$DATA like ?"
    val selectionArgs = arrayOf("$folderPath%")

    var count = 0

    val cursor: Cursor? = context.contentResolver.query(
        EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )

    cursor?.use {
        while (it.moveToNext()) {
            val filePath = it.getString(it.getColumnIndexOrThrow(DATA))
            val file = File(filePath)
            if (file.exists() && file.parent == folderPath) {
                count++
            }
        }
    }

    return count
}

// for converting bytes to MB and GB
fun getFormattedFileSize(sizeInBytes: Long): String {
    if (sizeInBytes <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (ln(sizeInBytes.toDouble()) / ln(1024.0)).toInt()

    val sizeInUnit = sizeInBytes / 1024.0.pow(digitGroups.toDouble())
    return "%.1f %s".format(sizeInUnit, units[digitGroups])
}


@SuppressLint("UseCompatTextViewDrawableApis")
fun setTextViewColorsForChangingSelection(
    context: Context,
    textViews: Array<TextView>,
    textColorId: Int,
    clickable: Boolean
) {
    val redColor = ContextCompat.getColor(context, textColorId)
    textViews.forEachIndexed { _, textView ->
        textView.setTextColor(redColor)
        textView.compoundDrawableTintList = ColorStateList.valueOf(redColor)
        textView.isClickable = clickable
    }
}

fun getFormattedDate(epochTime: Long): String {
    val date = Date(epochTime * 1000)
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    return dateFormat.format(date)
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

private fun getAlbumArtThumbnail(context: Context, musicFilePath: String): Uri? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(musicFilePath)
    val albumArtBytes = retriever.embeddedPicture
    return if (albumArtBytes != null) {
        val file = File.createTempFile("album_art", null, context.cacheDir)
        file.writeBytes(albumArtBytes)
        Uri.fromFile(file)
    } else {
        null
    }
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
                PlayerActivity.musicPosition =
                    PlayerActivity.musicListPlayerActivity.size - 1
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


fun checkPlayListData(playList: ArrayList<MusicDataClass>): ArrayList<MusicDataClass> {
    playList.forEachIndexed { index, musicData ->
        val file = File(musicData.path)
        if (!file.exists()) {
            playList.removeAt(index)
        }
    }
    return playList
}

fun updateTextViewWithItemCount(itemCount:Int): String {
    val itemCountText = if (itemCount == 1 || itemCount == 0) {
        "$itemCount song"
    } else {
        "$itemCount songs"
    }

    return itemCountText
}



fun notifyAdapterSongTextPosition() {

    if (SearchActivity.isInitialized) {
        SearchActivity.musicAdapter.notifyDataSetChanged()
    }
    if (PlayListActivity.isInitialized) {
        PlayListActivity.musicAdapter.notifyDataSetChanged()
    }
    if (FavouriteActivity.isInitialized) {
        FavouriteActivity.musicAdapter.notifyDataSetChanged()
    }
}


fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

