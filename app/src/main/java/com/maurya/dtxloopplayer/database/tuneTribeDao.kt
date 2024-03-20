package com.maurya.dtxloopplayer.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface tuneTribeDao {

    // Methods for MusicDataClass
    //single File

    @Insert
    suspend fun insertMusicDataSingle(musicData: MusicDataClass)

    @Update
    suspend fun updateMusicDataSingle(musicData: MusicDataClass)

    @Delete
    fun deleteMusicDataSingle(musicData: MusicDataClass)


    //list
    @Insert
    suspend fun insertMusicData(musicData: List<MusicDataClass>)

    @Query("SELECT * FROM musicRecords")
    fun getAllMusicData(): LiveData<List<MusicDataClass>>


    @Query("SELECT * FROM musicRecords WHERE musicName LIKE :query")
    fun searchMusicData(query: String): LiveData<List<MusicDataClass>>

    @Delete
    fun deleteMusicData(musicData: List<MusicDataClass>)


    @Update
    suspend fun updateMusicData(musicList: ArrayList<MusicDataClass>)




    // Methods for FolderDataClass
    @Insert
    suspend fun insertFolderData(folderData: FolderDataClass)

    @Query("SELECT * FROM folderRecords")
    fun getAllFolderData(): List<FolderDataClass>

    @Query("SELECT * FROM folderRecords WHERE folderName LIKE :query")
    fun searchFolderData(query: String): List<FolderDataClass>

    @Delete
    fun deleteFolderData(folderData: FolderDataClass)

    @Update
    fun updateFolderData(folderData: FolderDataClass)

    // Methods for PlaylistDataClass
    @Insert
    suspend fun insertPlaylistData(playlistData: PlayListDataClass)

    @Query("SELECT * FROM playlistRecords")
    fun getAllPlaylistData(): List<PlayListDataClass>

    @Query("SELECT * FROM playlistRecords WHERE playlistName LIKE :query")
    fun searchPlaylistData(query: String): List<PlayListDataClass>

    @Delete
    fun deletePlaylistData(playlistData: PlayListDataClass)

    @Update
    fun updatePlaylistData(playlistData: PlayListDataClass)



}
