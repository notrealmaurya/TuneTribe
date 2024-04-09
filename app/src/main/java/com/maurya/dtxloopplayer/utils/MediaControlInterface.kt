package com.maurya.dtxloopplayer.utils

import com.maurya.dtxloopplayer.database.MusicDataClass

interface MediaControlInterface {
    fun onSongSelected(musicList: ArrayList<MusicDataClass>, position: Int)
    fun onSongShuffled(musicList: ArrayList<MusicDataClass>, shuffle: Boolean)


}
