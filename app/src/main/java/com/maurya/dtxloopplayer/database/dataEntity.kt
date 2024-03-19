package com.maurya.dtxloopplayer.database

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

data class MusicDataClass(
    val id: String,
    val musicName: String,
    var folderName: String,
    val durationText: Long,
    val size: String,
    val albumArtist: String,
    val path: String,
    var image: String,
    val dateModified: Long,
    var isChecked: Boolean = false
)


data class FolderDataClass(
    val id: String,
    val folderName: String,
    val folderPath: String,
    val folderItemCount: Int,
    var isChecked: Boolean = false
)

data class PlayListDataClass(
    val id: String,
    val playListName: String,
    val dateModified: Long,
    val itemCount: Int,
    var isChecked: Boolean = false
)




