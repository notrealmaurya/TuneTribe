package com.maurya.dtxloopplayer.utils

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.maurya.dtxloopplayer.database.MusicDataClass
import com.maurya.dtxloopplayer.database.PlayListDataClass
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferenceHelper @Inject constructor(@ApplicationContext context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences(context.packageName, AppCompatActivity.MODE_PRIVATE)

    private val editor = sharedPreferences.edit()
    private val keyTheme = "theme"
    var theme
        get() = sharedPreferences.getInt(keyTheme, 2)
        set(value) {
            editor.putInt(keyTheme, value)
            editor.apply()
        }

    val themeFlag = arrayOf(
        AppCompatDelegate.MODE_NIGHT_NO,
        AppCompatDelegate.MODE_NIGHT_YES,
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )


    fun saveSortingOrder(sortingOrder: String) {
        sharedPreferences.edit().putString("sorting_order", sortingOrder).apply()
    }

    fun getSortingOrder(): String? {
        return sharedPreferences.getString("sorting_order", null)
    }

    fun getPlayerActivityTheme(): String? {
        return sharedPreferences.getString("playerActivity_theme", null)
    }

    fun setPlayerActivityTheme(theme: String) {
        sharedPreferences.edit().putString("playerActivity_theme", theme).apply()
    }

    fun savePlayList(playList: List<PlayListDataClass>) {
        val newPlaylistJson = Gson().toJson(playList)
        sharedPreferences.edit().putString("playListData", newPlaylistJson).apply()
    }

    fun getPlayList(): List<PlayListDataClass> {
        val playlistsJson = sharedPreferences.getString("playListData", null)
        return try {
            Gson().fromJson(playlistsJson, object : TypeToken<List<PlayListDataClass>>() {}.type)
                ?: listOf()
        } catch (e: Exception) {
            emptyList()
        }
    }


    fun savePlayListSong(playlistSong: List<MusicDataClass>, playListId: String) {
        val key = "playlist_$playListId"
        val newPlaylistJson = Gson().toJson(playlistSong)
        sharedPreferences.edit().putString(key, newPlaylistJson).apply()
    }

    fun getPlayListSong(playListId: String): List<MusicDataClass> {
        val key = "playlist_$playListId"
        val playlistSongJson = sharedPreferences.getString(key, null)
        return if (playlistSongJson != null) {
            try {
                Gson().fromJson(
                    playlistSongJson,
                    object : TypeToken<List<MusicDataClass>>() {}.type
                ) ?: listOf()
            } catch (e: Exception) {
                Log.e("shareItemClass", "Error parsing JSON for playlist ID: $playListId", e)
                listOf()
            }
        } else {
            listOf()
        }
    }


    fun savePlayListSongCount(count: Int, playListId: String) {
        val key = "playlistCount_$playListId"
        sharedPreferences.edit().putInt(key, count).apply()
    }

    fun getPlayListSongCount(playListId: String): Int {
        val key = "playlistCount_$playListId"
        return sharedPreferences.getInt(key, 0)
    }

}

