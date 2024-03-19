package com.maurya.dtxloopplayer.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SharedPreferenceHelper(context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences(context.packageName, AppCompatActivity.MODE_PRIVATE)


    private val editor = sharedPreferences.edit()
    private val keyTheme="theme"
    var theme get() = sharedPreferences.getInt(keyTheme,2)
        set(value) {
            editor.putInt(keyTheme,value)
            editor.commit()
        }

    val themeFlag= arrayOf(
        AppCompatDelegate.MODE_NIGHT_NO,
        AppCompatDelegate.MODE_NIGHT_YES,
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )

    fun savePlaylistData(data: String) {
        sharedPreferences.edit().putString("MusicPlaylist", data).apply()
    }

    fun getPlaylistData(): String? {
        return sharedPreferences.getString("MusicPlaylist", null)
    }


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


}