package com.maurya.dtxloopplayer

import android.content.Context

class SharedPreferenceHelper(context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

    fun savePlaylistData(data: String) {
        sharedPreferences.edit().putString("MusicPlaylist", data).apply()
    }

    fun getPlaylistData(): String? {
        return sharedPreferences.getString("MusicPlaylist", null)
    }

    fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean("dark_mode_enabled", false)
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("dark_mode_enabled", enabled).apply()
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