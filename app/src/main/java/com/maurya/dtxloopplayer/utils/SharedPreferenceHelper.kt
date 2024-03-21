package com.maurya.dtxloopplayer.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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


    fun savePlayList(playlist: ArrayList<PlayListDataClass>) {
        val gson = Gson()
        val newPlaylistJson = gson.toJson(playlist)

        sharedPreferences.edit().putString("playListData", newPlaylistJson).apply()
    }

    fun getPlayList(): ArrayList<PlayListDataClass> {
        val gson = Gson()
        val playlistsJson = sharedPreferences.getString("playListData", null)
        return if (playlistsJson != null) {
            val playlistType = object : TypeToken<ArrayList<PlayListDataClass>>() {}.type
            gson.fromJson(playlistsJson, playlistType)
        } else {
            arrayListOf()
        }
    }


}

