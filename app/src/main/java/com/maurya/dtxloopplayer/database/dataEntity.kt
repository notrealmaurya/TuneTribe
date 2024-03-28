package com.maurya.dtxloopplayer.database

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.room.Entity
import androidx.room.PrimaryKey

data class MusicDataClass(
    var id: String,
    var musicName: String,
    var folderName: String,
    var durationText: Long,
    var size: String,
    var albumArtist: String,
    var path: String,
    var image: String,
    var dateModified: Long,
    var isChecked: Boolean = false,
    var isFavourite: Boolean = false
)


data class FolderDataClass(
    @PrimaryKey val id: String,
    val folderName: String,
    val folderPath: String,
    val folderItemCount: Int,
    var isChecked: Boolean = false
)

data class PlayListDataClass(
    @PrimaryKey val id: String,
    var playListName: String,
    var dateModified: Long,
    var itemCount: Int,
    var isChecked: Boolean = false
)


data class PathDataClass(
    var path: String
)

