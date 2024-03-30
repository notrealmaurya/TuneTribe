package com.maurya.dtxloopplayer.utils

import com.maurya.dtxloopplayer.database.MusicDataClass

interface MediaControlInterface {
    fun onSongSelected(musicList: ArrayList<MusicDataClass>, position: Int)
    fun onSongShuffled(musicList: ArrayList<MusicDataClass>, shuffle: Boolean)
    fun onAddToQueue(song: MusicDataClass)

    // first: force play, second: restore song
//    fun onAddAlbumToQueue(songs: List<MusicDataClass>?, forcePlay: Pair<Boolean, MusicDataClass?>)
//    fun onUpdatePlayingAlbumSongs(songs: List<MusicDataClass>?)
//    fun onPlaybackSpeedToggled()
//    fun onHandleCoverOptionsUpdate()
//    fun onUpdatePositionFromNP(position: Int)

}
