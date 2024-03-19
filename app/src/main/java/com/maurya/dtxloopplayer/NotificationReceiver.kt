package com.maurya.dtxloopplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.maurya.dtxloopplayer.activities.PlayerActivity
import com.maurya.dtxloopplayer.fragments.NowPlayingBottomFragment
import com.maurya.dtxloopplayer.utils.exitApplication
import com.maurya.dtxloopplayer.utils.favouriteChecker
import com.maurya.dtxloopplayer.utils.notifyAdapterSongTextPosition
import com.maurya.dtxloopplayer.utils.setSongPosition

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        when (intent?.action) {
            ApplicationClass.PREVIOUS -> if (PlayerActivity.musicListPlayerActivity.size > 1) {
                prevNextSong(increment = false, context = context!!)
            }

            ApplicationClass.PLAY -> {
                if (PlayerActivity.isPlaying) pauseMusic() else playMusic(context!!)
            }

            ApplicationClass.NEXT -> if (PlayerActivity.musicListPlayerActivity.size > 1) {
                prevNextSong(increment = true, context = context!!)
            }

            ApplicationClass.EXIT -> {
                exitApplication()
            }
        }
    }

    private fun playMusic(context: Context) {
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.icon_pause, "Pause")
        PlayerActivity.binding.playPausePlayerActivity.setImageResource(R.drawable.icon_pause)
        NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.playPauseMiniPlayer.setImageResource(R.drawable.icon_pause)
    }

    private fun pauseMusic() {
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.icon_play, "Play")
        PlayerActivity.binding.playPausePlayerActivity.setImageResource(R.drawable.icon_play)
        NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.playPauseMiniPlayer.setImageResource(R.drawable.icon_play )
    }

    private fun prevNextSong(increment: Boolean, context: Context) {
        setSongPosition(increment = increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        Glide.with(context)
            .load(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].image)
            .apply(RequestOptions().placeholder(R.drawable.icon_music).centerCrop())
            .into(PlayerActivity.binding.songImagePlayerActivity)

        PlayerActivity.binding.songNAME.text =
            PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].musicName
        PlayerActivity.binding.songARTIST.text =
            PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].albumArtist
        Glide.with(context)
            .load(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].image)
            .apply(RequestOptions().placeholder(R.drawable.icon_music).centerCrop())
            .into(NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.AlbumArtMiniPlayer)

        NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.songNameMiniPlayer.text =
            PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].folderName
        NowPlayingBottomFragment.fragmentNowPlayingBottomBinding.songArtistMiniPlayer.text =
            PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].albumArtist

        playMusic(context)
        PlayerActivity.favouriteIndex = favouriteChecker(PlayerActivity.musicListPlayerActivity[PlayerActivity.musicPosition].id)
        if(PlayerActivity.isFavourite) PlayerActivity.binding.addFavouritePlayerActivity.setImageResource(R.drawable.icon_favourite_added)
        else PlayerActivity.binding.addFavouritePlayerActivity.setImageResource(R.drawable.icon_favourite_empty)

        notifyAdapterSongTextPosition()
    }
}